package com.cyanrocks.boilerplate.security.authentication.handler;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cyanrocks.boilerplate.constants.ErrorCodeEnum;
import com.cyanrocks.boilerplate.dao.entity.UserToken;
import com.cyanrocks.boilerplate.dao.mapper.UserTokenMapper;
import com.cyanrocks.boilerplate.vo.response.GenericResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
@Component
public class DefaultExpiredSessionStrategy implements SessionInformationExpiredStrategy {

    public static final Logger LOG = LoggerFactory.getLogger(DefaultExpiredSessionStrategy.class);

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserTokenMapper userTokenMapper;

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent sessionInformationExpiredEvent)
        throws IOException, ServletException {
        if (LOG.isInfoEnabled()) {
            LOG.info("session is expired");
        }
        userTokenMapper.delete(Wrappers.<UserToken>lambdaQuery().lt(UserToken::getExpireTime, LocalDateTime.now()));
        HttpServletResponse response = sessionInformationExpiredEvent.getResponse();
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
            GenericResponse.bizError(ErrorCodeEnum.SESSION_EXPIRED.getCode(), "expired session", null)));
    }
}
