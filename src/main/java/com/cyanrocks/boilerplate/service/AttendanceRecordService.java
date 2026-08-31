package com.cyanrocks.boilerplate.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cyanrocks.boilerplate.dao.entity.AttendanceRecord;
import com.cyanrocks.boilerplate.dao.entity.AttendanceUser;
import com.cyanrocks.boilerplate.dao.mapper.AttendanceRecordMapper;
import com.cyanrocks.boilerplate.dao.mapper.AttendanceOvertimeMapper;
import com.cyanrocks.boilerplate.dao.mapper.AttendanceLeaveMapper;
import com.cyanrocks.boilerplate.dao.mapper.AttendanceUserMapper;
import com.cyanrocks.boilerplate.utils.DingUtils;
import com.cyanrocks.boilerplate.utils.PageUtils;
import com.cyanrocks.boilerplate.vo.dto.AttendanceDailySummaryDTO;
import com.cyanrocks.boilerplate.vo.response.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 打卡结果业务类（工时看板 第4步）
 * 从 attendance_user 表取考勤组人员，分批调用钉钉 /attendance/list 获取打卡结果并落库
 * @Author yangshihao
 * @Date 2026/8/18
 */
@Service
public class AttendanceRecordService {

    private static final Logger LOG = LoggerFactory.getLogger(AttendanceRecordService.class);

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /** 钉钉接口单次最大 userId 数 */
    private static final int USER_BATCH_SIZE = 50;
    /** 钉钉接口单次最大条数 */
    private static final long PAGE_LIMIT = 50L;
    /** 钉钉接口单次查询最大日期跨度（天，含边界） */
    private static final int MAX_DATE_SPAN_DAYS = 7;
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    @Autowired
    private AttendanceUserMapper attendanceUserMapper;
    @Autowired
    private AttendanceRecordMapper attendanceRecordMapper;
    @Autowired
    private AttendanceOvertimeMapper attendanceOvertimeMapper;
    @Autowired
    private AttendanceLeaveMapper attendanceLeaveMapper;
    @Autowired
    private DingUtils dingUtils;

    /**
     * 同步指定日期（可跨多天，自动按7天切分）的打卡结果
     * @param startDate 起始日期（含）
     * @param endDate 结束日期（含）
     * @return 本次新增/更新的记录条数
     */
    public int syncAttendanceRecords(LocalDate startDate, LocalDate endDate) {
        int total = 0;
        LocalDate windowStart = startDate;
        while (!windowStart.isAfter(endDate)) {
            LocalDate windowEnd = windowStart.plusDays(MAX_DATE_SPAN_DAYS - 1L);
            if (windowEnd.isAfter(endDate)) {
                windowEnd = endDate;
            }
            total += syncOneWindow(windowStart, windowEnd);
            windowStart = windowEnd.plusDays(1);
        }
        LOG.info("打卡结果同步完成: {} ~ {}, 共处理 {} 条", startDate, endDate, total);
        return total;
    }

    /**
     * 同步单个日期的打卡结果
     */
    public int syncAttendanceRecords(LocalDate date) {
        return syncAttendanceRecords(date, date);
    }

    /**
     * 同步一个查询窗口（跨度不超过7天）
     */
    private int syncOneWindow(LocalDate startDate, LocalDate endDate) {
        // 1. 取考勤组人员
        List<AttendanceUser> attendanceUsers = attendanceUserMapper.selectList(null);
        if (CollectionUtil.isEmpty(attendanceUsers)) {
            LOG.warn("考勤组人员为空，请先执行考勤组人员同步任务");
            return 0;
        }
        List<String> allUserIds = new ArrayList<>();
        for (AttendanceUser attendanceUser : attendanceUsers) {
            allUserIds.add(attendanceUser.getDingUserId());
        }

        String workDateFrom = startDate.atTime(0, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String workDateTo = endDate.atTime(23, 59, 59).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        int count = 0;
        // 2. 按批次分批调用（单批最多50人）
        for (int i = 0; i < allUserIds.size(); i += USER_BATCH_SIZE) {
            List<String> batchUserIds = allUserIds.subList(i, Math.min(i + USER_BATCH_SIZE, allUserIds.size()));
            count += syncOneBatch(batchUserIds, workDateFrom, workDateTo);
        }
        LOG.info("打卡结果窗口同步完成: {} ~ {}, 处理 {} 条", startDate, endDate, count);
        return count;
    }

    /**
     * 同步一批用户的打卡结果（内部处理offset分页）
     * 含死循环防护：空页终止 + 重复页检测
     */
    private int syncOneBatch(List<String> userIds, String workDateFrom, String workDateTo) {
        int count = 0;
        long offset = 0L;
        // 上一页首条记录ID，用于检测接口忽略offset导致的重复页
        Long lastFirstRecordId = null;
        // 防御性上限：50人*每天若干条，正常一页即可
        for (int page = 0; page < 100; page++) {
            JSONObject body = dingUtils.getAttendanceRecords(workDateFrom, workDateTo, userIds, offset, PAGE_LIMIT);
            if (null == body) {
                LOG.error("获取打卡结果失败: userIds数量={}, workDateFrom={}", userIds.size(), workDateFrom);
                return count;
            }
            JSONArray recordResults = body.getJSONArray("recordresult");
            boolean hasMore = body.getBool("hasMore", false);
            // 防护1：空页但hasMore=true，接口分页异常，立即终止防止空转
            if (recordResults == null || recordResults.isEmpty()) {
                if (hasMore) {
                    LOG.warn("打卡结果分页异常: 返回空页但hasMore=true，终止翻页. offset={}, workDateFrom={}, userIds={}",
                            offset, workDateFrom, userIds.size());
                }
                break;
            }
            // 防护2：与上一页首条记录相同，说明接口忽略了offset参数（每次返回同一页），立即终止
            JSONObject firstRecord = (JSONObject) recordResults.get(0);
            Long firstRecordId = firstRecord.getLong("recordId");
            if (firstRecordId != null && firstRecordId.equals(lastFirstRecordId)) {
                LOG.warn("打卡结果分页异常: 第{}页与上一页数据重复(接口忽略offset?)，终止翻页. offset={}, workDateFrom={}",
                        page + 1, offset, workDateFrom);
                break;
            }
            lastFirstRecordId = firstRecordId;
            for (Object obj : recordResults) {
                JSONObject record = (JSONObject) obj;
                if (upsertRecord(record)) {
                    count++;
                }
            }
            // hasMore分页
            if (hasMore) {
                offset += PAGE_LIMIT;
            } else {
                break;
            }
        }
        return count;
    }

    /**
     * 单条打卡记录入库：按 recordId 判断新增或更新
     * @return true=新增或更新成功
     */
    private boolean upsertRecord(JSONObject record) {
        // recordId缺失时（未打卡/请假等SYSTEM生成记录），用接口返回的id字段兜底，同为唯一标识
        Long recordId = record.getLong("recordId");
        if (recordId == null) {
            recordId = record.getLong("id");
        }
        if (recordId == null) {
            LOG.warn("打卡记录缺少recordId和id，跳过: {}", record);
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        AttendanceRecord entity = attendanceRecordMapper.selectOne(
                Wrappers.<AttendanceRecord>lambdaQuery().eq(AttendanceRecord::getRecordId, recordId));
        boolean isInsert = (entity == null);
        if (isInsert) {
            entity = new AttendanceRecord();
            entity.setRecordId(recordId);
            entity.setCreateTime(now);
        }
        entity.setPlanId(record.getLong("planId"));
        entity.setDingUserId(record.getStr("userId"));
        entity.setGroupId(record.getLong("groupId"));
        entity.setWorkDate(toLocalDateTime(record.getLong("workDate")));
        entity.setCheckType(record.getStr("checkType"));
        entity.setTimeResult(record.getStr("timeResult"));
        entity.setLocationResult(record.getStr("locationResult"));
        entity.setBaseCheckTime(toLocalDateTime(record.getLong("baseCheckTime")));
        entity.setUserCheckTime(toLocalDateTime(record.getLong("userCheckTime")));
        entity.setSourceType(record.getStr("sourceType"));
        entity.setUpdateTime(now);
        if (isInsert) {
            attendanceRecordMapper.insert(entity);
        } else {
            attendanceRecordMapper.updateById(entity);
        }
        return true;
    }

    /**
     * 毫秒时间戳转LocalDateTime（钉钉返回的毫秒时间戳，按东八区解析）
     */
    private LocalDateTime toLocalDateTime(Long millis) {
        if (millis == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZONE);
    }

    /**
     * 每日工时汇总分页查询（基于工作日，上下班都有的记录）
     * @param startDate 起始工作日（含），可为null
     * @param endDate 结束工作日（含），可为null
     */
    public PageResult<AttendanceDailySummaryDTO> queryDailySummary(LocalDate startDate, LocalDate endDate,
                                                                   int pageNum, int pageSize) {
        LocalDateTime startTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endTime = (endDate != null) ? endDate.plusDays(1).atStartOfDay() : null;
        PageResult<AttendanceDailySummaryDTO> result = PageUtils.of(pageNum, pageSize,
                () -> attendanceRecordMapper.countDailySummary(startTime, endTime),
                (offset, size) -> attendanceRecordMapper.selectDailySummary(startTime, endTime, offset, size));
        // 原始统计SQL不改动，部门/打卡结果/加班信息在内存中合并
        fillDeptInfo(result.getList());
        fillTimeResult(result.getList());
        fillOvertime(result.getList());
        fillLeave(result.getList());
        return result;
    }

    /**
     * 填充上下班打卡结果（time_result）
     * 与统计SQL的MAX(user_check_time)语义对齐：取当日最晚一次打卡的结果
     */
    private void fillTimeResult(List<AttendanceDailySummaryDTO> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        java.util.Set<String> dingUserIds = new java.util.HashSet<>();
        LocalDate minDay = null;
        LocalDate maxDay = null;
        for (AttendanceDailySummaryDTO dto : list) {
            if (dto.getDingUserId() != null) {
                dingUserIds.add(dto.getDingUserId());
            }
            if (dto.getWorkDay() != null) {
                if (minDay == null || dto.getWorkDay().isBefore(minDay)) {
                    minDay = dto.getWorkDay();
                }
                if (maxDay == null || dto.getWorkDay().isAfter(maxDay)) {
                    maxDay = dto.getWorkDay();
                }
            }
        }
        if (dingUserIds.isEmpty() || minDay == null) {
            return;
        }
        // 查询本页用户+日期范围内的上下班打卡记录
        List<AttendanceRecord> records = attendanceRecordMapper.selectList(
                Wrappers.<AttendanceRecord>lambdaQuery()
                        .in(AttendanceRecord::getDingUserId, dingUserIds)
                        .ge(AttendanceRecord::getWorkDate, minDay.atStartOfDay())
                        .lt(AttendanceRecord::getWorkDate, maxDay.plusDays(1).atStartOfDay())
                        .in(AttendanceRecord::getCheckType, java.util.Arrays.asList("OnDuty", "OffDuty")));
        // key: dingUserId|workDay|checkType -> 最晚打卡的记录
        java.util.Map<String, AttendanceRecord> bestMap = new java.util.HashMap<>();
        for (AttendanceRecord record : records) {
            if (record.getDingUserId() == null || record.getWorkDate() == null || record.getCheckType() == null) {
                continue;
            }
            String key = record.getDingUserId() + "|" + record.getWorkDate().toLocalDate() + "|" + record.getCheckType();
            AttendanceRecord current = bestMap.get(key);
            if (current == null || (record.getUserCheckTime() != null
                    && (current.getUserCheckTime() == null || record.getUserCheckTime().isAfter(current.getUserCheckTime())))) {
                bestMap.put(key, record);
            }
        }
        // 回填到DTO
        for (AttendanceDailySummaryDTO dto : list) {
            AttendanceRecord onDuty = bestMap.get(dto.getDingUserId() + "|" + dto.getWorkDay() + "|OnDuty");
            AttendanceRecord offDuty = bestMap.get(dto.getDingUserId() + "|" + dto.getWorkDay() + "|OffDuty");
            dto.setOnDutyTimeResult(onDuty != null ? onDuty.getTimeResult() : null);
            dto.setOffDutyTimeResult(offDuty != null ? offDuty.getTimeResult() : null);
        }
    }

    /**
     * 填充所属部门信息（来自 attendance_user 表的 dept_id/dept_name）
     * 只处理本页涉及的用户
     */
    private void fillDeptInfo(List<AttendanceDailySummaryDTO> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        java.util.Set<String> dingUserIds = new java.util.HashSet<>();
        for (AttendanceDailySummaryDTO dto : list) {
            if (dto.getDingUserId() != null) {
                dingUserIds.add(dto.getDingUserId());
            }
        }
        if (dingUserIds.isEmpty()) {
            return;
        }
        List<com.cyanrocks.boilerplate.dao.entity.AttendanceUser> attendanceUsers =
                attendanceUserMapper.selectList(Wrappers.<com.cyanrocks.boilerplate.dao.entity.AttendanceUser>lambdaQuery()
                        .in(com.cyanrocks.boilerplate.dao.entity.AttendanceUser::getDingUserId, dingUserIds));
        java.util.Map<String, com.cyanrocks.boilerplate.dao.entity.AttendanceUser> userMap = new java.util.HashMap<>();
        for (com.cyanrocks.boilerplate.dao.entity.AttendanceUser attendanceUser : attendanceUsers) {
            userMap.put(attendanceUser.getDingUserId(), attendanceUser);
        }
        for (AttendanceDailySummaryDTO dto : list) {
            com.cyanrocks.boilerplate.dao.entity.AttendanceUser attendanceUser = userMap.get(dto.getDingUserId());
            if (attendanceUser != null) {
                dto.setDeptId(attendanceUser.getDeptId());
                dto.setDeptName(attendanceUser.getDeptName());
            }
        }
    }

    /**
     * 填充当日加班信息到汇总结果（是否加班 + 开始/结束时间）
 * 跨天加班按覆盖的每一天命中；多条加班取最早开始、最晚结束
     * 只处理本页涉及的用户和日期范围
     */
    private void fillOvertime(List<AttendanceDailySummaryDTO> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        // 先默认不加班
        for (AttendanceDailySummaryDTO dto : list) {
            dto.setOvertime(false);
        }
        // 收集本页涉及的钉钉userId和日期范围
        java.util.Set<String> dingUserIds = new java.util.HashSet<>();
        LocalDate minDay = null;
        LocalDate maxDay = null;
        for (AttendanceDailySummaryDTO dto : list) {
            if (dto.getDingUserId() != null) {
                dingUserIds.add(dto.getDingUserId());
            }
            if (dto.getWorkDay() != null) {
                if (minDay == null || dto.getWorkDay().isBefore(minDay)) {
                    minDay = dto.getWorkDay();
                }
                if (maxDay == null || dto.getWorkDay().isAfter(maxDay)) {
                    maxDay = dto.getWorkDay();
                }
            }
        }
        if (dingUserIds.isEmpty() || minDay == null) {
            return;
        }
        // 查询与本页日期范围重叠的加班审批
        List<com.cyanrocks.boilerplate.dao.entity.AttendanceOvertime> overtimes = attendanceOvertimeMapper.selectList(
                Wrappers.<com.cyanrocks.boilerplate.dao.entity.AttendanceOvertime>lambdaQuery()
                        .in(com.cyanrocks.boilerplate.dao.entity.AttendanceOvertime::getDingUserId, dingUserIds)
                        .lt(com.cyanrocks.boilerplate.dao.entity.AttendanceOvertime::getStartTime, maxDay.plusDays(1).atStartOfDay())
                        .gt(com.cyanrocks.boilerplate.dao.entity.AttendanceOvertime::getEndTime, minDay.atStartOfDay()));
        if (CollectionUtil.isEmpty(overtimes)) {
            return;
        }
        // key: dingUserId|workDay -> [最早开始时间, 最晚结束时间]
        java.util.Map<String, LocalDateTime[]> overtimeMap = new java.util.HashMap<>();
        for (com.cyanrocks.boilerplate.dao.entity.AttendanceOvertime overtime : overtimes) {
            if (overtime.getDingUserId() == null || overtime.getStartTime() == null || overtime.getEndTime() == null) {
                continue;
            }
            // 加班区间覆盖的每一天都标记为加班日
            LocalDate day = overtime.getStartTime().toLocalDate();
            LocalDate lastDay = overtime.getEndTime().toLocalDate();
            for (; !day.isAfter(lastDay); day = day.plusDays(1)) {
                String key = overtime.getDingUserId() + "|" + day;
                LocalDateTime[] times = overtimeMap.get(key);
                if (times == null) {
                    overtimeMap.put(key, new LocalDateTime[]{overtime.getStartTime(), overtime.getEndTime()});
                } else {
                    // 多条加班：取最早开始、最晚结束
                    if (overtime.getStartTime().isBefore(times[0])) {
                        times[0] = overtime.getStartTime();
                    }
                    if (overtime.getEndTime().isAfter(times[1])) {
                        times[1] = overtime.getEndTime();
                    }
                }
            }
        }
        // 回填到DTO
        for (AttendanceDailySummaryDTO dto : list) {
            LocalDateTime[] times = overtimeMap.get(dto.getDingUserId() + "|" + dto.getWorkDay());
            if (times != null) {
                dto.setOvertime(true);
                dto.setOvertimeStartTime(times[0]);
                dto.setOvertimeEndTime(times[1]);
            }
        }
    }

    /**
     * 填充当日请假信息到汇总结果（是否请假 + 开始/结束时间）
     * 判断口径：请假区间与工作日当天有交集即为true，跨天请假按覆盖的每一天命中
     * 多条请假取最早开始、最晚结束；只处理本页涉及的用户和日期范围，不做时长计算
     */
    private void fillLeave(List<AttendanceDailySummaryDTO> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        // 先默认未请假
        for (AttendanceDailySummaryDTO dto : list) {
            dto.setLeave(false);
        }
        // 收集本页涉及的钉钉userId和日期范围
        java.util.Set<String> dingUserIds = new java.util.HashSet<>();
        LocalDate minDay = null;
        LocalDate maxDay = null;
        for (AttendanceDailySummaryDTO dto : list) {
            if (dto.getDingUserId() != null) {
                dingUserIds.add(dto.getDingUserId());
            }
            if (dto.getWorkDay() != null) {
                if (minDay == null || dto.getWorkDay().isBefore(minDay)) {
                    minDay = dto.getWorkDay();
                }
                if (maxDay == null || dto.getWorkDay().isAfter(maxDay)) {
                    maxDay = dto.getWorkDay();
                }
            }
        }
        if (dingUserIds.isEmpty() || minDay == null) {
            return;
        }
        // 查询与本页日期范围有交集的请假记录
        List<com.cyanrocks.boilerplate.dao.entity.AttendanceLeave> leaves = attendanceLeaveMapper.selectList(
                Wrappers.<com.cyanrocks.boilerplate.dao.entity.AttendanceLeave>lambdaQuery()
                        .in(com.cyanrocks.boilerplate.dao.entity.AttendanceLeave::getDingUserId, dingUserIds)
                        .lt(com.cyanrocks.boilerplate.dao.entity.AttendanceLeave::getLeaveStartTime, maxDay.plusDays(1).atStartOfDay())
                        .gt(com.cyanrocks.boilerplate.dao.entity.AttendanceLeave::getLeaveEndTime, minDay.atStartOfDay()));
        if (CollectionUtil.isEmpty(leaves)) {
            return;
        }
        // key: dingUserId|workDay -> [最早开始时间, 最晚结束时间]
        java.util.Map<String, LocalDateTime[]> leaveMap = new java.util.HashMap<>();
        for (com.cyanrocks.boilerplate.dao.entity.AttendanceLeave leave : leaves) {
            if (leave.getDingUserId() == null || leave.getLeaveStartTime() == null || leave.getLeaveEndTime() == null) {
                continue;
            }
            // 请假区间覆盖的每一天都标记为请假
            LocalDate day = leave.getLeaveStartTime().toLocalDate();
            LocalDate lastDay = leave.getLeaveEndTime().toLocalDate();
            for (; !day.isAfter(lastDay); day = day.plusDays(1)) {
                String key = leave.getDingUserId() + "|" + day;
                LocalDateTime[] times = leaveMap.get(key);
                if (times == null) {
                    leaveMap.put(key, new LocalDateTime[]{leave.getLeaveStartTime(), leave.getLeaveEndTime()});
                } else {
                    // 多条请假：取最早开始、最晚结束
                    if (leave.getLeaveStartTime().isBefore(times[0])) {
                        times[0] = leave.getLeaveStartTime();
                    }
                    if (leave.getLeaveEndTime().isAfter(times[1])) {
                        times[1] = leave.getLeaveEndTime();
                    }
                }
            }
        }
        // 回填到DTO
        for (AttendanceDailySummaryDTO dto : list) {
            LocalDateTime[] times = leaveMap.get(dto.getDingUserId() + "|" + dto.getWorkDay());
            if (times != null) {
                dto.setLeave(true);
                dto.setLeaveStartTime(times[0]);
                dto.setLeaveEndTime(times[1]);
            }
        }
    }
}
