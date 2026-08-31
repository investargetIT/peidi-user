//package com.cyanrocks.boilerplate.service;
//
//import com.cyanrocks.boilerplate.dao.entity.AttendanceRecord;
//import com.cyanrocks.boilerplate.dao.mapper.AttendanceRecordMapper;
//import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//
///**
// * 打卡结果同步测试（真实调用钉钉接口，写入数据库）
// * 前置条件：attendance_user 表已有考勤组人员（可先跑 AttendanceUserSyncTest）
// */
//@SpringBootTest
//public class AttendanceRecordSyncTest {
//
//    private static final Logger LOG = LoggerFactory.getLogger(AttendanceRecordSyncTest.class);
//
//    /** 直接改这里：要同步的日期范围（含首尾，跨度不限，内部自动按7天切分） */
//    private static final LocalDate SYNC_START_DATE = LocalDate.of(2026, 8, 21);
//    private static final LocalDate SYNC_END_DATE = LocalDate.of(2026, 8, 22);
//
//    @Autowired
//    private AttendanceRecordService attendanceRecordService;
//
//    @Autowired
//    private AttendanceRecordMapper attendanceRecordMapper;
//
//    /**
//     * 同步 SYNC_START_DATE ~ SYNC_END_DATE 指定日期范围内的打卡结果并打印入库数据
//     */
//    @Test
//    public void testSyncAttendanceRecords() {
//        LOG.info("开始同步 {} ~ {} 的打卡结果", SYNC_START_DATE, SYNC_END_DATE);
//
//        int count = attendanceRecordService.syncAttendanceRecords(SYNC_START_DATE, SYNC_END_DATE);
//        LOG.info("同步完成，处理记录数: {}", count);
//
//        // 查询该范围入库数据校验
//        LocalDateTime rangeStart = SYNC_START_DATE.atStartOfDay();
//        LocalDateTime rangeEnd = SYNC_END_DATE.plusDays(1).atStartOfDay();
//        List<AttendanceRecord> records = attendanceRecordMapper.selectList(
//                com.baomidou.mybatisplus.core.toolkit.Wrappers.<AttendanceRecord>lambdaQuery()
//                        .ge(AttendanceRecord::getWorkDate, rangeStart)
//                        .lt(AttendanceRecord::getWorkDate, rangeEnd)
//                        .orderByAsc(AttendanceRecord::getWorkDate)
//                        .orderByAsc(AttendanceRecord::getDingUserId)
//                        .orderByAsc(AttendanceRecord::getBaseCheckTime));
//        LOG.info("{} ~ {} 入库打卡记录共 {} 条", SYNC_START_DATE, SYNC_END_DATE, records.size());
//        for (AttendanceRecord record : records) {
//            LOG.info("userId={}, checkType={}, timeResult={}, workDate={}, userCheckTime={}, sourceType={}, recordId={}",
//                    record.getDingUserId(), record.getCheckType(), record.getTimeResult(),
//                    record.getWorkDate(), record.getUserCheckTime(), record.getSourceType(), record.getRecordId());
//        }
//        assert !records.isEmpty() : "同步后打卡记录不应为空";
//    }
//}
