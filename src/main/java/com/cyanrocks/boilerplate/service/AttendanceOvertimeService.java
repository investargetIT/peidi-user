package com.cyanrocks.boilerplate.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cyanrocks.boilerplate.dao.entity.AttendanceOvertime;
import com.cyanrocks.boilerplate.dao.mapper.AttendanceOvertimeMapper;
import com.cyanrocks.boilerplate.utils.DingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 加班审批业务类（工时看板 支线逻辑1）
 * 拉取钉钉「加班申请」审批（COMPLETED状态），
 * 从审批详情表单中提取加班人/开始时间/结束时间，按审批实例ID upsert落库
 * @Author yangshihao
 * @Date 2026/8/20
 */
@Service
public class AttendanceOvertimeService {

    private static final Logger LOG = LoggerFactory.getLogger(AttendanceOvertimeService.class);

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    /** 审批表单日期控件格式：2026-08-15 09:00 */
    private static final DateTimeFormatter FORM_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** 加班申请审批模板code（工时看板文档260819.md 第7章） */
    @Value("${attendance.overtime.process-code:PROC-E955D439-1EF2-4DA6-AB61-4F286F2F474F}")
    private String OVERTIME_PROCESS_CODE;

    @Autowired
    private AttendanceOvertimeMapper attendanceOvertimeMapper;
    @Autowired
    private DingUtils dingUtils;

    /**
     * 同步指定时间范围内发起的已完成加班审批
     * 注意：ListProcessInstanceIds的时间过滤维度是「审批发起时间」，不是加班时间本身
     * @param startDate 起始日期（含），按审批发起时间
     * @param endDate 结束日期（含），按审批发起时间
     * @return 本次新增/更新的记录条数
     */
    public int syncOvertimeApprovals(LocalDate startDate, LocalDate endDate) {
        long startTimeMilli = startDate.atStartOfDay(ZONE).toInstant().toEpochMilli();
        long endTimeMilli = endDate.plusDays(1).atStartOfDay(ZONE).toInstant().toEpochMilli() - 1;
        List<String> instanceIds = dingUtils.getProcessInstanceIds(startTimeMilli, endTimeMilli, OVERTIME_PROCESS_CODE);
        if (null == instanceIds) {
            LOG.error("加班审批同步失败: 获取审批实例ID列表失败, {} ~ {}", startDate, endDate);
            return 0;
        }
        int count = 0;
        for (String instanceId : instanceIds) {
            try {
                count += upsertOvertime(instanceId);
            } catch (Exception e) {
                LOG.error("加班审批入库异常: processInstanceId={}, 原因: {}", instanceId, e.getMessage());
            }
        }
        LOG.info("加班审批同步完成: {} ~ {}, 实例数={}, 入库行数={}", startDate, endDate, instanceIds.size(), count);
        return count;
    }

    /**
     * 单条加班审批入库：一条审批可能包含多个加班人，按人拆分为多行
     * upsert依据：审批实例ID + 钉钉userId 组合
     * @return 入库行数（每个加班人一行）
     */
    private int upsertOvertime(String processInstanceId) {
        JSONObject detail = dingUtils.getProcessInstanceDetail(processInstanceId);
        if (null == detail) {
            LOG.warn("获取加班审批详情失败: processInstanceId={}", processInstanceId);
            return 0;
        }
        JSONArray components = detail.getJSONArray("formComponentValues");
        if (null == components || components.isEmpty()) {
            LOG.warn("加班审批表单为空: processInstanceId={}", processInstanceId);
            return 0;
        }
        // 提取表单控件：partner=加班人（可能多个）, startTime=开始时间, finishTime=结束时间
        java.util.List<String[]> partners = new java.util.ArrayList<>();
        String fallbackUsername = null;
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        for (Object obj : components) {
            JSONObject component = (JSONObject) obj;
            String bizAlias = component.getStr("bizAlias");
            if ("partner".equals(bizAlias)) {
                fallbackUsername = component.getStr("value");
                partners = extractPartners(component.getStr("extValue"));
            } else if ("startTime".equals(bizAlias)) {
                startTime = parseFormTime(component.getStr("value"));
            } else if ("finishTime".equals(bizAlias)) {
                endTime = parseFormTime(component.getStr("value"));
            }
        }
        if (null == startTime || null == endTime) {
            LOG.warn("加班审批缺少开始/结束时间，跳过: processInstanceId={}", processInstanceId);
            return 0;
        }
        long durationSeconds = Math.max(0, Duration.between(startTime, endTime).getSeconds());
        if (partners.isEmpty()) {
            // extValue解析失败时兜底：整条审批按单人入库，靠姓名关联
            LOG.warn("加班审批未解析到加班人明细，按单人兜底入库: processInstanceId={}, username={}",
                    processInstanceId, fallbackUsername);
            upsertOne(processInstanceId, null, fallbackUsername, startTime, endTime, durationSeconds);
            return 1;
        }
        // 每个加班人一行
        int count = 0;
        for (String[] partner : partners) {
            // partner[0]=emplId(钉钉userId), partner[1]=姓名
            upsertOne(processInstanceId, partner[0], partner[1], startTime, endTime, durationSeconds);
            count++;
        }
        return count;
    }

    /**
     * 单人单行upsert：按 审例ID+钉钉userId 判断新增或更新
     */
    private void upsertOne(String processInstanceId, String dingUserId, String username,
                           LocalDateTime startTime, LocalDateTime endTime, long durationSeconds) {
        LocalDateTime now = LocalDateTime.now();
        AttendanceOvertime entity = attendanceOvertimeMapper.selectOne(
                Wrappers.<AttendanceOvertime>lambdaQuery()
                        .eq(AttendanceOvertime::getProcessInstanceId, processInstanceId)
                        .eq(dingUserId != null, AttendanceOvertime::getDingUserId, dingUserId)
                        .isNull(dingUserId == null, AttendanceOvertime::getDingUserId));
        boolean isInsert = (entity == null);
        if (isInsert) {
            entity = new AttendanceOvertime();
            entity.setProcessInstanceId(processInstanceId);
            entity.setDingUserId(dingUserId);
            entity.setCreateTime(now);
        }
        entity.setUsername(username);
        entity.setStartTime(startTime);
        entity.setEndTime(endTime);
        entity.setDurationSeconds(durationSeconds);
        entity.setUpdateTime(now);
        if (isInsert) {
            attendanceOvertimeMapper.insert(entity);
        } else {
            attendanceOvertimeMapper.updateById(entity);
        }
    }

    /**
     * 从partner组件的extValue JSON数组里提取所有加班人
     * 格式：[{"emplId":"17197987055634865","name":"段英姿",...},...]
     * @return 每项为[emplId(钉钉userId), 姓名]
     */
    private java.util.List<String[]> extractPartners(String extValue) {
        java.util.List<String[]> partners = new java.util.ArrayList<>();
        if (StrUtil.isBlank(extValue)) {
            return partners;
        }
        try {
            JSONArray arr = JSONUtil.parseArray(extValue);
            for (Object obj : arr) {
                JSONObject partner = (JSONObject) obj;
                String emplId = partner.getStr("emplId");
                String name = partner.getStr("name");
                if (StrUtil.isNotBlank(emplId) || StrUtil.isNotBlank(name)) {
                    partners.add(new String[]{emplId, name});
                }
            }
        } catch (Exception e) {
            LOG.warn("解析加班人extValue失败: {}", extValue);
        }
        return partners;
    }

    /**
     * 解析表单日期控件值（2026-08-15 09:00）
     */
    private LocalDateTime parseFormTime(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, FORM_TIME_FMT);
        } catch (Exception e) {
            LOG.warn("解析加班时间失败: {}", value);
            return null;
        }
    }
}
