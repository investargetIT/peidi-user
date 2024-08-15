package com.cyanrocks.boilerplate.security.authentication.handler;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cyanrocks.boilerplate.dao.entity.User;
import com.cyanrocks.boilerplate.dao.entity.UserToken;
import com.cyanrocks.boilerplate.dao.mapper.UserTokenMapper;
import com.cyanrocks.boilerplate.security.authentication.UserInfoDetails;
import com.cyanrocks.boilerplate.vo.response.GenericResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserTokenMapper userTokenMapper;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
        throws IOException {
        if (logger.isInfoEnabled()) {
            logger.info("logout success");
        }
        //删除token
        UserInfoDetails userInfoDetails = (UserInfoDetails)authentication.getPrincipal();
        userTokenMapper.delete(Wrappers.<UserToken>lambdaQuery()
                .eq(UserToken::getUsername,userInfoDetails.getUserName())
                .eq(UserToken::getService,userInfoDetails.getService()));
        // 退出成功后返回 和前端约定的Json
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(GenericResponse.success()));
    }
}
