package com.cyanrocks.boilerplate.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cyanrocks.boilerplate.dao.entity.AttendanceLeave;
import com.cyanrocks.boilerplate.dao.entity.AttendanceUser;
import com.cyanrocks.boilerplate.dao.mapper.AttendanceLeaveMapper;
import com.cyanrocks.boilerplate.dao.mapper.AttendanceUserMapper;
import com.cyanrocks.boilerplate.utils.DingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * 请假信息业务类（工时看板 支线逻辑3）
 * 拉取钉钉请假状态（/topapi/attendance/getleavestatus），
 * 落库 attendance_leave 表，不做时长计算
 * @Author yangshihao
 * @Date 2026/8/24
 */
@Service
public class AttendanceLeaveService {

    private static final Logger LOG = LoggerFactory.getLogger(AttendanceLeaveService.class);

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    /** 钉钉接口单次最大 userId 数 */
    private static final int USER_BATCH_SIZE = 100;
    /** 钉钉接口单页最大条数 */
    private static final long PAGE_SIZE = 20L;
    /** 钉钉接口单次查询最大日期跨度（天，含边界） */
    private static final int MAX_DATE_SPAN_DAYS = 180;
    /** 防御性翻页上限：100人窗口内正常页数有限 */
    private static final int MAX_PAGE = 500;

    @Autowired
    private AttendanceUserMapper attendanceUserMapper;
    @Autowired
    private AttendanceLeaveMapper attendanceLeaveMapper;
    @Autowired
    private DingUtils dingUtils;

    /**
     * 同步指定时间范围内的请假记录（可跨多天，自动按180天切分）
     * @param startDate 起始日期（含）
     * @param endDate 结束日期（含）
     * @return 本次新增/更新的记录条数
     */
    public int syncLeaveRecords(LocalDate startDate, LocalDate endDate) {
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
        LOG.info("请假信息同步完成: {} ~ {}, 共处理 {} 条", startDate, endDate, total);
        return total;
    }

    /**
     * 同步一个查询窗口（跨度不超过180天）
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

        long startTimeMilli = startDate.atStartOfDay(ZONE).toInstant().toEpochMilli();
        long endTimeMilli = endDate.plusDays(1).atStartOfDay(ZONE).toInstant().toEpochMilli() - 1;

        int count = 0;
        // 2. 按批次分批调用（单批最多100人）
        for (int i = 0; i < allUserIds.size(); i += USER_BATCH_SIZE) {
            List<String> batchUserIds = allUserIds.subList(i, Math.min(i + USER_BATCH_SIZE, allUserIds.size()));
            count += syncOneBatch(String.join(",", batchUserIds), startTimeMilli, endTimeMilli);
        }
        LOG.info("请假信息窗口同步完成: {} ~ {}, 处理 {} 条", startDate, endDate, count);
        return count;
    }

    /**
     * 同步一批用户的请假记录（内部处理offset分页）
     * 含死循环防护：空页终止
     */
    private int syncOneBatch(String userIdList, long startTimeMilli, long endTimeMilli) {
        int count = 0;
        long offset = 0L;
        for (int page = 0; page < MAX_PAGE; page++) {
            JSONObject body = dingUtils.getLeaveStatus(userIdList, startTimeMilli, endTimeMilli, offset, PAGE_SIZE);
            if (null == body) {
                LOG.error("获取请假状态失败: userIds数量={}, startTimeMilli={}", userIdList.split(",").length, startTimeMilli);
                return count;
            }
            JSONObject result = body.getJSONObject("result");
            if (null == result) {
                break;
            }
            JSONArray leaveStatus = result.getJSONArray("leave_status");
            boolean hasMore = result.getBool("has_more", false);
            // 防护：空页但has_more=true，接口分页异常，立即终止防止空转
            if (leaveStatus == null || leaveStatus.isEmpty()) {
                if (hasMore) {
                    LOG.warn("请假状态分页异常: 返回空页但has_more=true，终止翻页. offset={}", offset);
                }
                break;
            }
            for (Object obj : leaveStatus) {
                JSONObject leave = (JSONObject) obj;
                if (upsertLeave(leave)) {
                    count++;
                }
            }
            // has_more分页
            if (hasMore) {
                offset += PAGE_SIZE;
            } else {
                break;
            }
        }
        return count;
    }

    /**
     * 单条请假记录入库：按 钉钉userId+开始时间+结束时间+leaveCode 判断新增或更新
     * @return true=新增或更新成功
     */
    private boolean upsertLeave(JSONObject leave) {
        String dingUserId = leave.getStr("userid");
        Long startTimeMilli = leave.getLong("start_time");
        Long endTimeMilli = leave.getLong("end_time");
        if (dingUserId == null || startTimeMilli == null || endTimeMilli == null) {
            LOG.warn("请假记录缺少关键字段，跳过: {}", leave);
            return false;
        }
        String leaveCode = leave.getStr("leave_code");
        LocalDateTime startTime = toLocalDateTime(startTimeMilli);
        LocalDateTime endTime = toLocalDateTime(endTimeMilli);
        LocalDateTime now = LocalDateTime.now();
        AttendanceLeave entity = attendanceLeaveMapper.selectOne(
                Wrappers.<AttendanceLeave>lambdaQuery()
                        .eq(AttendanceLeave::getDingUserId, dingUserId)
                        .eq(AttendanceLeave::getLeaveStartTime, startTime)
                        .eq(AttendanceLeave::getLeaveEndTime, endTime)
                        .eq(leaveCode != null, AttendanceLeave::getLeaveCode, leaveCode)
                        .isNull(leaveCode == null, AttendanceLeave::getLeaveCode));
        boolean isInsert = (entity == null);
        if (isInsert) {
            entity = new AttendanceLeave();
            entity.setDingUserId(dingUserId);
            entity.setLeaveStartTime(startTime);
            entity.setLeaveEndTime(endTime);
            entity.setLeaveCode(leaveCode);
            entity.setCreateTime(now);
        }
        entity.setDurationUnit(leave.getStr("duration_unit"));
        entity.setDurationPercent(leave.getInt("duration_percent"));
        entity.setUpdateTime(now);
        if (isInsert) {
            attendanceLeaveMapper.insert(entity);
        } else {
            attendanceLeaveMapper.updateById(entity);
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
}
