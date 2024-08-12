package com.cyanrocks.boilerplate.user.security.authentication.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tobee.brains.common.model.response.GenericResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 默认的退出成功处理器
 */
@Component("defaultLogoutSuccessHandler")
public class DefaultLogoutSuccessHandler implements LogoutSuccessHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
        throws IOException {
        if (logger.isInfoEnabled()) {
            logger.info("logout success");
        }
        // 退出成功后返回 和前端约定的Json
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(GenericResponse.success()));
    }
}
