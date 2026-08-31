//package com.cyanrocks.boilerplate.service;
//
//import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
///**
// * 部门架构同步测试（真实调用钉钉接口，写入数据库）
// * 前置条件：数据库已建表 attendance_dept，attendance_user 已加 dept_id/dept_name 字段
// */
//@SpringBootTest
//public class AttendanceDeptSyncTest {
//
//    private static final Logger LOG = LoggerFactory.getLogger(AttendanceDeptSyncTest.class);
//
//    @Autowired
//    private AttendanceDeptService attendanceDeptService;
//
//    /**
//     * 同步部门树并回填考勤人员所属部门
//     */
//    @Test
//    public void testSyncDepartments() {
//        LOG.info("开始同步部门树");
//
//        int count = attendanceDeptService.syncDepartments();
//        LOG.info("同步完成，部门数: {}", count);
//
//        // 打印部门组织架构树
//        LOG.info("部门组织架构树: {}", cn.hutool.json.JSONUtil.toJsonStr(attendanceDeptService.listDeptTree()));
//    }
//}
