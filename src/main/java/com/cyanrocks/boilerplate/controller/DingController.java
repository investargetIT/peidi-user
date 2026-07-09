package com.cyanrocks.boilerplate.controller;

import cn.hutool.json.JSONObject;
import com.cyanrocks.boilerplate.utils.DingUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author wjq
 * @Date 2024/10/15 15:21
 */
@RestController
@RequestMapping("/ding")
@Api(tags = {"钉钉接口"})
public class DingController {

    @Autowired
    private DingUtils dingUtils;

    @GetMapping("/userInfo")
    @ApiOperation(value = "获取用户信息")
    public JSONObject getDingUserInfo(@RequestParam(value="code") String code) {
        String userId = dingUtils.getUseridByCode(code);
        return dingUtils.getUserinfoByUserid(userId);
    }

    @GetMapping("/userInfoById")
    @ApiOperation(value = "通过id获取用户信息")
    public JSONObject getDingUserInfoById(@RequestParam(value="userId") String userId) {
        return dingUtils.getUserinfoByUserid(userId);
    }

    @GetMapping("/department")
    @ApiOperation(value = "获取部门详情")
    public JSONObject getDingDepartment(@RequestParam(value="deptId") Long deptId) {
        return dingUtils.getDingDepartment(deptId);
    }

    @GetMapping("/parentbyuser")
    @ApiOperation(value = "根据用户钉钉id获取上级部门列表")
    public JSONObject getDingParentbyuser(@RequestParam(value="userId") String userId) {
        return dingUtils.getDingParentbyuser(userId);
    }

    @GetMapping("/jsapi")
    @ApiOperation(value = "获取JSAPI鉴权")
    public JSONObject getDingJsapi(@RequestParam(value="nonceStr") String nonceStr,@RequestParam(value="url") String url) {
        return dingUtils.getDingJsapi(nonceStr, url);
    }

    @GetMapping("/sendPmMessage")
    @ApiOperation(value = "发送pm系统消息")
    public void sendPmMessage(@RequestParam(value="type") Integer type,
                              @RequestParam(value="message", required = false) String message,
                              @RequestParam(value="taskId", required = false) Long taskId,
                              @RequestParam(value="userList") String[] userList) {
        dingUtils.sendPmMessage(type, message, taskId, userList);
    }

    @GetMapping("/creatTodoTask")
    @ApiOperation(value = "发送待办任务")
    public String creatTodoTask(@RequestParam(value="userId") String userId) {
        return dingUtils.creatTodoTask(userId);
    }

    @GetMapping("/deleteTodoTask")
    @ApiOperation(value = "删除待办任务")
    public void deleteTodoTask(@RequestParam(value="taskId") String taskId) {
        dingUtils.deleteTodoTask(taskId);
    }

    @GetMapping("/departmentUsers")
    @ApiOperation(value = "获取部门用户列表（分页）")
    public JSONObject getDepartmentUsers(@RequestParam(value="deptId") Long deptId,
                                        @RequestParam(value="cursor", required = false) Long cursor,
                                        @RequestParam(value="size", required = false) Integer size) {
        return dingUtils.getDepartmentUsers(deptId, cursor, size);
    }

    @GetMapping("/allDepartmentUsers")
    @ApiOperation(value = "获取部门所有用户（自动分页）")
    public JSONObject getAllDepartmentUsers(@RequestParam(value="deptId") Long deptId) {
        return dingUtils.getDepartmentUsers(deptId, null, null);
    }

}
