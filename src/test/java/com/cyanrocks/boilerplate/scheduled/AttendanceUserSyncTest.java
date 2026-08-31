//package com.cyanrocks.boilerplate.scheduled;
//
//import com.cyanrocks.boilerplate.dao.entity.AttendanceUser;
//import com.cyanrocks.boilerplate.dao.mapper.AttendanceUserMapper;
//import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.List;
//
///**
// * 考勤组人员同步测试（真实调用钉钉接口）
// */
//@SpringBootTest
//public class AttendanceUserSyncTest {
//
//    private static final Logger LOG = LoggerFactory.getLogger(AttendanceUserSyncTest.class);
//
//    @Autowired
//    private ScheduledTasks scheduledTasks;
//
//    @Autowired
//    private AttendanceUserMapper attendanceUserMapper;
//
//    @Test
//    public void testSyncAttendanceUsers() {
//        // 执行同步前库中数据
//        List<AttendanceUser> before = attendanceUserMapper.selectList(null);
//        LOG.info("同步前库中人数: {}", before.size());
//
//        // 调用定时任务同步方法
//        scheduledTasks.syncAttendanceUsers();
//
//        // 同步后校验
//        List<AttendanceUser> after = attendanceUserMapper.selectList(null);
//        LOG.info("同步后库中人数: {}", after.size());
//        for (AttendanceUser user : after) {
//            LOG.info("考勤人员: dingUserId={}, groupId={}, createTime={}",
//                    user.getDingUserId(), user.getGroupId(), user.getCreateTime());
//        }
//        assert !after.isEmpty() : "同步后考勤人员不应为空";
//    }
//}
