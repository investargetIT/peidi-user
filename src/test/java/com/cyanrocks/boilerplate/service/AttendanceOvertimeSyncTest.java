//package com.cyanrocks.boilerplate.service;
//
//import com.baomidou.mybatisplus.core.toolkit.Wrappers;
//import com.cyanrocks.boilerplate.dao.entity.AttendanceOvertime;
//import com.cyanrocks.boilerplate.dao.mapper.AttendanceOvertimeMapper;
//import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.time.LocalDate;
//import java.util.List;
//
///**
// * 加班审批同步测试（真实调用钉钉接口，写入数据库）
// * 前置条件：数据库已建表 attendance_overtime
// */
//@SpringBootTest
//public class AttendanceOvertimeSyncTest {
//
//    private static final Logger LOG = LoggerFactory.getLogger(AttendanceOvertimeSyncTest.class);
//
//    /** 直接改这里：要同步的审批发起日期范围（含首尾） */
//    private static final LocalDate SYNC_START_DATE = LocalDate.of(2026, 3, 1);
//    private static final LocalDate SYNC_END_DATE = LocalDate.of(2026, 8, 25);
//
//    @Autowired
//    private AttendanceOvertimeService attendanceOvertimeService;
//
//    @Autowired
//    private AttendanceOvertimeMapper attendanceOvertimeMapper;
//
//    /**
//     * 同步指定日期范围内发起的已完成加班审批并打印入库数据
//     */
//    @Test
//    public void testSyncOvertimeApprovals() {
//        LOG.info("开始同步 {} ~ {} 的加班审批", SYNC_START_DATE, SYNC_END_DATE);
//
//        int count = attendanceOvertimeService.syncOvertimeApprovals(SYNC_START_DATE, SYNC_END_DATE);
//        LOG.info("同步完成，处理记录数: {}", count);
//
//        // 查询入库数据校验
//        List<AttendanceOvertime> records = attendanceOvertimeMapper.selectList(
//                Wrappers.<AttendanceOvertime>lambdaQuery()
//                        .orderByAsc(AttendanceOvertime::getStartTime));
//        LOG.info("入库加班审批共 {} 条", records.size());
//        for (AttendanceOvertime record : records) {
//            LOG.info("username={}, dingUserId={}, startTime={}, endTime={}, durationSeconds={}, processInstanceId={}",
//                    record.getUsername(), record.getDingUserId(), record.getStartTime(),
//                    record.getEndTime(), record.getDurationSeconds(), record.getProcessInstanceId());
//        }
//    }
//}
