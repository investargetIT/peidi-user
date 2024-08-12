package com.cyanrocks.boilerplate.user.security.authentication.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tobee.brains.common.model.response.GenericResponse;
import com.tobee.brains.user.validate.errorcode.UserErrorCodeEnum;
import com.tobee.brains.user.validate.exception.BadValidateCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author lmh
 * @Date 2020/8/3 8:32 下午
 * @Version 1.0
 **/
@Component("defaultLoginFailureHandler")
public class DefaultLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultLoginFailureHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException exception) throws IOException, ServletException {
        if (LOG.isInfoEnabled()) {
            LOG.info("login failed");
        }
        response.setContentType("application/json;charset=UTF-8");
        int errorCode;
        String errorMsg;
        // 所有的认证异常都可以在这里添加，目前只支持用户名密码错误异常
        if (exception instanceof BadCredentialsException || exception instanceof UsernameNotFoundException) {
            errorCode = UserErrorCodeEnum.USERNAME_OR_PASSWORD_ERROR.getCode();
            errorMsg = "username or password error";
        } else if (exception instanceof BadValidateCodeException) {
            errorCode = UserErrorCodeEnum.BAD_VALIDATE_CODE_EXCEPTION.getCode();
            errorMsg = "invalid code";
        } else {
            errorCode = GenericResponse.ERROR_CODE;
            errorMsg = GenericResponse.ERROR_MSG;
        }
        response.getWriter()
            .write(objectMapper.writeValueAsString(GenericResponse.bizError(errorCode, errorMsg, null)));
    }

}
