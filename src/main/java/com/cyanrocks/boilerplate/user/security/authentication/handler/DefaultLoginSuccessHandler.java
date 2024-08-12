package com.cyanrocks.boilerplate.user.security.authentication.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tobee.brains.common.model.response.GenericResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
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
@Component("defaultLoginSuccessHandler")
@Primary
public class DefaultLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {
        if (log.isInfoEnabled()) {
            log.info("login success");
        }
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(GenericResponse.success()));
    }

}
