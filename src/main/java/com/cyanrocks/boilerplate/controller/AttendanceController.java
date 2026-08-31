package com.cyanrocks.boilerplate.controller;

import com.cyanrocks.boilerplate.service.AttendanceDeptService;
import com.cyanrocks.boilerplate.service.AttendanceRecordService;
import com.cyanrocks.boilerplate.vo.dto.AttendanceDailySummaryDTO;
import com.cyanrocks.boilerplate.vo.response.PageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 工时看板接口
 */
@RestController
@RequestMapping("/attendance")
@Api(tags = {"工时看板接口"})
public class AttendanceController {

    @Autowired
    private AttendanceRecordService attendanceRecordService;
    @Autowired
    private AttendanceDeptService attendanceDeptService;

    /**
     * 每日工时汇总分页查询（按工作日）
     * @param startDate 起始工作日（含），格式yyyy-MM-dd，可选
     * @param endDate 结束工作日（含），格式yyyy-MM-dd，可选
     * @param pageNum 页码，从1开始，默认1
     * @param pageSize 每页条数，默认10
     */
    @GetMapping("/dailySummary")
    @ApiOperation(value = "每日工时汇总分页查询")
    public PageResult<AttendanceDailySummaryDTO> dailySummary(
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        return attendanceRecordService.queryDailySummary(startDate, endDate, pageNum, pageSize);
    }

    /**
     * 部门组织架构树（嵌套children结构，顶层为根部门的直属子部门）
     */
    @GetMapping("/dept/tree")
    @ApiOperation(value = "部门组织架构树")
    public List<Map<String, Object>> deptTree() {
        return attendanceDeptService.listDeptTree();
    }
}
