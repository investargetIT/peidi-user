package com.cyanrocks.boilerplate.user.security.authentication.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tobee.brains.common.model.response.GenericResponse;
import com.tobee.brains.user.validate.errorcode.UserErrorCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author lmh
 * @Date 2020/8/20 11:16 上午
 * @Version 1.0
 **/
@Component
public class DefaultInvalidSessionStrategy implements InvalidSessionStrategy {

    public static final Logger LOG = LoggerFactory.getLogger(DefaultInvalidSessionStrategy.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onInvalidSessionDetected(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
        throws IOException, ServletException {
        if (LOG.isInfoEnabled()) {
            LOG.info("session is invalid");
        }
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        httpServletResponse.getWriter().write(objectMapper.writeValueAsString(
            GenericResponse.bizError(UserErrorCodeEnum.SESSION_INVALID.getCode(), "invalid session", null)));
    }
}
