package com.cyanrocks.boilerplate.security.authentication.handler;

import com.cyanrocks.boilerplate.dao.entity.UserToken;
import com.cyanrocks.boilerplate.dao.mapper.UserTokenMapper;
import com.cyanrocks.boilerplate.security.authentication.UserInfoDetails;
import com.cyanrocks.boilerplate.vo.response.GenericResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.time.LocalDateTime;

/**
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
@Component("defaultLoginSuccessHandler")
@Primary
public class DefaultLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserTokenMapper userTokenMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {
        if (log.isInfoEnabled()) {
            log.info("login success");
        }
        //token表生成记录
        UserInfoDetails userInfoDetails = (UserInfoDetails)authentication.getPrincipal();
        UserToken userToken = new UserToken();
        userToken.setService(userInfoDetails.getService());
        userToken.setUsername(userInfoDetails.getUserName());
        userToken.setExpireTime(LocalDateTime.now().plusDays(1));//登录状态有效期1天
        userTokenMapper.insert(userToken);
        String token = userToken.getId().toString();
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(GenericResponse.success(token)));
    }

}
