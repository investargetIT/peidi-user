package com.cyanrocks.boilerplate.security.authentication.password;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cyanrocks.boilerplate.dao.entity.User;
import com.cyanrocks.boilerplate.dao.mapper.UserMapper;
import com.cyanrocks.boilerplate.security.authentication.UserInfoDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
@Component("passwordLoginUserDetailService")
public class PasswordLoginUserDetailService implements UserDetailsService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String principal) throws AuthenticationException {
        String username = principal.split("&")[0];
        String service = principal.split("&")[1];
        if (logger.isInfoEnabled()) {
            logger.info("用户名密码方式登陆{}, 帐号={}", service, username);
        }
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getEmail,username).or().eq(User::getMobile,username));
        if (null == user) {
            throw new UsernameNotFoundException(String.format("%s user not exist", username));
        }
        UserInfoDetails userInfoDetails = new UserInfoDetails();
        userInfoDetails.setUserId(user.getId());
        userInfoDetails.setUserName(user.getUsername());
        userInfoDetails.setPhone(user.getMobile());
        userInfoDetails.setEmail(user.getEmail());
        userInfoDetails.setPassword(user.getPassword());
        userInfoDetails.setService(service);
        return userInfoDetails;
    }
}
