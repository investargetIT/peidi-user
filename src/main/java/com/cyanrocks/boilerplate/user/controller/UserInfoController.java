package com.cyanrocks.boilerplate.user.controller;

import com.tobee.brains.user.facade.UserFacade;
import com.tobee.brains.user.model.request.UpdateUserInfoRequest;
import com.tobee.brains.user.model.vo.UserInfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
@RestController
@RequestMapping("/api/v1/user/info")
@Api(value = "用户信息操作接口", tags = {"用户信息操作接口"})
public class UserInfoController {

    @Autowired
    private UserFacade userFacade;

    @GetMapping("/current-user")
    @ApiOperation(value = "获取认证通过的用户的信息")
    public UserInfoVO getCurrentUser() {
        return userFacade.getCurrentUser();
    }

    @PostMapping("/update-user")
    @ApiOperation(value = "更新用户信息")
    public UserInfoVO updateCurrentUserInfo(@RequestBody UpdateUserInfoRequest request) {
        return userFacade.updateCurrentUserInfo(request);
    }
}
