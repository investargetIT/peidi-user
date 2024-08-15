package com.cyanrocks.boilerplate.security.authentication.handler;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cyanrocks.boilerplate.constants.ErrorCodeEnum;
import com.cyanrocks.boilerplate.dao.entity.UserToken;
import com.cyanrocks.boilerplate.dao.mapper.UserTokenMapper;
import com.cyanrocks.boilerplate.vo.response.GenericResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
@Component
public class DefaultInvalidSessionStrategy implements InvalidSessionStrategy {

    public static final Logger LOG = LoggerFactory.getLogger(DefaultInvalidSessionStrategy.class);

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserTokenMapper userTokenMapper;

    @Override
    public void onInvalidSessionDetected(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
        throws IOException, ServletException {
        if (LOG.isInfoEnabled()) {
            LOG.info("session is invalid, token={}", httpServletRequest.getParameter("token"));
        }
        userTokenMapper.deleteById(httpServletRequest.getParameter("token"));
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        httpServletResponse.getWriter().write(objectMapper.writeValueAsString(
            GenericResponse.bizError(ErrorCodeEnum.SESSION_INVALID.getCode(), "invalid session", null)));
    }
}
