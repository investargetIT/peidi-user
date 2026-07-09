package com.cyanrocks.boilerplate.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cyanrocks.boilerplate.constants.ErrorCodeEnum;
import com.cyanrocks.boilerplate.constants.ValidateCodeTypeEnum;
import com.cyanrocks.boilerplate.dao.entity.User;
import com.cyanrocks.boilerplate.dao.entity.UserInfo;
import com.cyanrocks.boilerplate.dao.entity.UserSite;
import com.cyanrocks.boilerplate.dao.entity.UserToken;
import com.cyanrocks.boilerplate.dao.mapper.UserInfoMapper;
import com.cyanrocks.boilerplate.dao.mapper.UserMapper;
import com.cyanrocks.boilerplate.dao.mapper.UserSiteMapper;
import com.cyanrocks.boilerplate.dao.mapper.UserTokenMapper;
import com.cyanrocks.boilerplate.exception.BusinessException;
import com.cyanrocks.boilerplate.exception.IllegalArgException;
import com.cyanrocks.boilerplate.facade.UserFacade;
import com.cyanrocks.boilerplate.vo.request.*;
import com.cyanrocks.boilerplate.validate.service.ValidateCodeService;
import com.cyanrocks.boilerplate.vo.response.GenericResponse;
import com.cyanrocks.boilerplate.vo.response.UserCheckVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
@RestController
@RequestMapping("/user")
@Api(value = "注册和认证api", tags = {"Login接口"})
public class UserAuthController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private ValidateCodeService validateCodeService;

    @Autowired
    private UserTokenMapper userTokenMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private UserSiteMapper userSiteMapper;

    @PostMapping("/info")
    @ApiOperation(value = "更新用户信息")
    public void updateUserInfo(@RequestBody UserInfo req) {
//        if (null !=req.getHireDateStr() ){
//            LocalDate utcDate = Instant.ofEpochMilli(Long.parseLong(req.getHireDateStr()))
//                    .atZone(ZoneId.of("UTC"))
//                    .toLocalDate();
//            req.setHireDate(utcDate);
//        }
        UserInfo userInfoRes = userInfoMapper.selectOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUserId, req.getUserId()));
        if (null == userInfoRes) {
            req.setCreatedAt(LocalDateTime.now());
            req.setHireDate(LocalDate.now());
            userInfoMapper.insert(req);
        } else {
            req.setUpdatedAt(LocalDateTime.now());
            userInfoMapper.update(req, Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUserId, req.getUserId()));
        }
    }

    // 邮箱注册
    @PostMapping("/email-register")
    @ApiOperation(value = "使用邮箱注册账户")
    public void registerUserAccountByEmail(@Valid @RequestBody EmailRegistrationRequest registrationView) {
        userFacade.registerUserAccountByMail(registrationView);
    }

//    @PostMapping("/email-update-password")
//    @ApiOperation(value = "邮箱:利用旧密码更新密码")
//    public void updatePasswordByEmail(@RequestBody UpdatePasswordRequest updatePasswordCommand,
//                                      HttpServletRequest request, HttpServletResponse response) throws IOException {
//        userFacade.updatePasswordByEmail(updatePasswordCommand);
//        // // 更新密码后清除登陆状态
//        Cookie cookie = new Cookie("JSESSIONID", (String)null);
//        String cookiePath = request.getContextPath() + "/";
//        cookie.setPath(cookiePath);
//        cookie.setMaxAge(0);
//        response.addCookie(cookie);
//        response.setContentType("application/json;charset=UTF-8");
//        response.getWriter().write(objectMapper.writeValueAsString(GenericResponse.success()));
//    }

    // 手机注册
    @PostMapping("/sms-register")
    @ApiOperation(value = "使用手机注册账户")
    public void registerUserAccountBySms(@Valid @RequestBody SmsRegistrationRequest registrationView) {
        userFacade.registerUserAccountBySms(registrationView);
    }

    @PostMapping("/sms-update-password")
    @ApiOperation(value = "手机:利用旧密码更新密码")
    public void updatePasswordBySns(@Valid @RequestBody UpdatePasswordRequest updatePasswordCommand,
                                    HttpServletRequest request, HttpServletResponse response) throws IOException {
        userFacade.updatePasswordBySms(updatePasswordCommand);
        // // 更新密码后清除登陆状态
        Cookie cookie = new Cookie("JSESSIONID", (String) null);
        String cookiePath = request.getContextPath() + "/";
        cookie.setPath(cookiePath);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(GenericResponse.success()));
    }

    @PostMapping("/update-password")
    @ApiOperation(value = "利用旧密码更新密码")
    public void updatePasswordByOld(@Valid @RequestBody UpdatePasswordRequest updatePasswordCommand,
                                    HttpServletRequest request, HttpServletResponse response) throws IOException {
        userFacade.updatePasswordByOld(updatePasswordCommand);
        // // 更新密码后清除登陆状态
        Cookie cookie = new Cookie("JSESSIONID", (String) null);
        String cookiePath = request.getContextPath() + "/";
        cookie.setPath(cookiePath);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(GenericResponse.success()));
    }

    @PostMapping("/reset-password")
    @ApiOperation(value = "重置密码")
    public void resetPassword(@Valid @RequestBody ForgetPasswordRequest forgetPasswordCommand) {
        userFacade.resetPassword(forgetPasswordCommand);
    }

    /**
     * 当需要身份认证时，跳转到这里
     *
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping("/auth-require")
    public void requireAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                GenericResponse.bizError(ErrorCodeEnum.USER_NOT_LOGIN.getCode(), "user not login", null)));
    }

    @GetMapping("/user-check")
    @ApiOperation(value = "判断用户是否登陆")
    public UserCheckVO checkUser(@RequestParam String token, HttpServletRequest request, HttpServletResponse response) {
        UserToken userToken = userTokenMapper.selectById(token);
        if (null == userToken) {
            throw new BusinessException(ErrorCodeEnum.SESSION_INVALID.getCode(), "token 无效");
        }
        if (LocalDateTime.now().isAfter(userToken.getExpireTime())) {
            //token过期
            userTokenMapper.deleteById(token);
            UserCheckVO vo = new UserCheckVO();
            vo.setHasLoginUser(false);
            Cookie cookie = new Cookie("JSESSIONID", (String) null);
            String cookiePath = request.getContextPath() + "/";
            cookie.setPath(cookiePath);
            cookie.setMaxAge(0);
            response.addCookie(cookie);
            throw new BusinessException(ErrorCodeEnum.SESSION_EXPIRED.getCode(), "token 过期");
        }

        UserCheckVO vo = new UserCheckVO();
        vo.setHasLoginUser(true);
        vo.setService(userToken.getService());
        vo.setUsername(userToken.getUsername());
        vo.setId(userToken.getUserId());
        if (null != userToken.getUserId()) {
            System.out.println(userToken.getUserId());
            User user = userMapper.selectById(userToken.getUserId());
            vo.setDataSource(user.getDataSource());
            vo.setDeptId(user.getDeptId());
            vo.setDingId(user.getDingId());
        }
        return vo;
    }

    @GetMapping("/site")
    @ApiOperation(value = "获取基地列表")
    public List<UserSite> getSiteList() {
        return userSiteMapper.selectAll();
    }

    @PostMapping("/validate-code")
    @ApiOperation(value = "发送验证码")
    public void sendCode(@Valid @RequestBody ValidateCodeRequest validateCodeCommand) {
        validateCodeService.generateValidateCodeAndSend(validateCodeCommand.getDestination(),
                ValidateCodeTypeEnum.of(validateCodeCommand.getCodeType().toLowerCase()));
    }

    @GetMapping("/check-code")
    @ApiOperation(value = "校验验证码")
    public void checkCode(@RequestParam("mobile") String mobile, @RequestParam("mobileCode") String mobileCode) {
        validateCodeService.checkCodeEffective(mobile, mobileCode, ValidateCodeTypeEnum.SMS_OMS_ORDER_TRADE);
    }


    @PostMapping(value = "/login/password", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ApiOperation(value = "账号密码的登陆方式")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名", required = true, paramType = "form", dataType = "String"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, paramType = "form", dataType = "String")})
    public void loginByUsernameAndPassword(@RequestParam("username") String username,
                                           @RequestParam("password") String password) {
        // 不实现任何内容，只是为了出api文档
    }


    @GetMapping(value = "/user/list")
    @ApiOperation(value = "获取所有没有在oA上删除的用户名称和id")
    public List<User> userList(@RequestParam("name") String name) {
        List<User> userList = userMapper.selectList(Wrappers.<User>lambdaQuery().isNotNull(User::getDingId).eq(User::getOaDelete, 0).likeRight(User::getUsername, name).select(User::getId, User::getUsername, User::getJobNum));
        return userList;
    }

    @PostMapping(value = "/user/update")
    @ApiOperation(value = "通过UserId修改工号")
    public void updateUserJobNum(@RequestBody User user) {
        if (user.getId() != null && user.getJobNum() != null) {
            userMapper.update(null, Wrappers.<User>lambdaUpdate()
                    .set(User::getJobNum, user.getJobNum())
                    .eq(User::getId, user.getId()));
        }
    }


    @GetMapping(value = "/user/getDingdingId")
    @ApiOperation(value = "获取dingdingId通过名称")
    public String getDingdingId(@RequestParam("name") String name) {
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, name));
        return user != null ? user.getDingId() : null;
    }

}
