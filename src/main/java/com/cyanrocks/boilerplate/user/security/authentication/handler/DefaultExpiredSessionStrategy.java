package com.cyanrocks.boilerplate.user.security.authentication.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tobee.brains.common.model.response.GenericResponse;
import com.tobee.brains.user.validate.errorcode.UserErrorCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author lmh
 * @Date 2020/8/20 11:15 上午
 * @Version 1.0
 **/
@Component
public class DefaultExpiredSessionStrategy implements SessionInformationExpiredStrategy {

    public static final Logger LOG = LoggerFactory.getLogger(DefaultExpiredSessionStrategy.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent sessionInformationExpiredEvent)
        throws IOException, ServletException {
        if (LOG.isInfoEnabled()) {
            LOG.info("session is expired");
        }
        HttpServletResponse response = sessionInformationExpiredEvent.getResponse();
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
            GenericResponse.bizError(UserErrorCodeEnum.SESSION_EXPIRED.getCode(), "expired session", null)));
    }
}
