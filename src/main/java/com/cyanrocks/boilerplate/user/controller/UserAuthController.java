package com.cyanrocks.boilerplate.user.controller;

import com.cyanrocks.boilerplate.user.model.request.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Objects;

/**
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
@RestController
@RequestMapping("/auth")
@Api(value = "注册和认证api", tags = {"Login接口"})
public class UserAuthController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private ValidateCodeService validateCodeService;

    @PostMapping("/register")
    @ApiOperation(value = "注册账户")
    @ApiResponses({@ApiResponse(code = 100100001, message = "注册时用户已经存在"),
            @ApiResponse(code = 100100003, message = "无效的验证码")})
    public void registerUserAccount(@Valid @RequestBody final RegisterRequest registerRequest,
                                    HttpServletRequest httpServletRequest) {
        if (!UserValidator.validateRegisterRequest(registerRequest)) {
            throw new TobeePluginIllegalArgException();
        }

        userAuthFacade.registerUserAccount(registerRequest);
        userAuthFacade.autoLogIn(httpServletRequest, registerRequest.getIdentifier(), registerRequest.getPassword());
    }

    @PostMapping("/reset-password")
    @ApiOperation(value = "忘记密码：重置密码")
    @ApiResponses({@ApiResponse(code = 100100003, message = "无效的验证码"),
            @ApiResponse(code = 100100002, message = "该账户不存在")})
    public void resetPassword(@Valid @RequestBody final ResetPasswordRequest request) {
        if (!UserValidator.validateResetPasswordRequest(request)) {
            throw new TobeePluginIllegalArgException();
        }
        userAuthFacade.resetPassword(request);
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
            GenericResponse.bizError(UserErrorCodeEnum.USER_NOT_LOGIN.getCode(), "user not login", null)));
    }

    @GetMapping("/user-check")
    @ApiOperation(value = "判断用户是否登陆")
    public UserCheckVO checkUser(@AuthenticationPrincipal Authentication user) {
        if (Objects.isNull(user)) {
            UserCheckVO vo = new UserCheckVO();
            vo.setHasLoginUser(false);
            return vo;
        }
        UserInfoDetails userInfoDetails = (UserInfoDetails)user.getPrincipal();
        UserCheckVO vo = new UserCheckVO();
        vo.setHasLoginUser(true);
        vo.setIdentifier(userInfoDetails.getUsername());
        return vo;
    }

    @PostMapping("/validate-code")
    @ApiOperation(value = "发送验证码")
    public void sendCode(@Valid @RequestBody final ValidateCodeRequest validateCodeCommand) {
        validateCodeService.generateValidateCodeAndSend(validateCodeCommand.getDestination(),
            ValidateCodeTypeEnum.of(validateCodeCommand.getCodeType().toLowerCase()));
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

    @PostMapping(value = "/login/shopify")
    @ApiOperation(value = "Shopify的登录方式")
    public void loginShopify(@RequestBody ShopifyLoginRequest request) {
        // 不实现任何内容，只是为了出api文档
    }
}
