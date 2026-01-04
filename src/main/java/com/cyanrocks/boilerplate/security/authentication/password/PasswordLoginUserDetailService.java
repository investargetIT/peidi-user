package com.cyanrocks.boilerplate.security.authentication.password;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cyanrocks.boilerplate.dao.entity.User;
import com.cyanrocks.boilerplate.dao.mapper.UserMapper;
import com.cyanrocks.boilerplate.security.authentication.UserInfoDetails;
import org.apache.commons.lang3.StringUtils;
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
//        String site;
//        if (principal.split("&").length == 1 || StringUtils.isEmpty(principal.split("&")[1]) || "null".equals(principal.split("&")[1])){
//            //默认佩蒂杭州
//            site = "3";
//        }else {
//            site = principal.split("&")[1];
//        }

        if (logger.isInfoEnabled()) {
            logger.info("用户名密码方式登陆, 帐号={}", principal);
        }
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getEmail,username).ne(User::getOaDelete,true));
        if (null == user){
            user = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getMobile,username).ne(User::getOaDelete,true));
        }
        if (null == user) {
            throw new UsernameNotFoundException(String.format("%s user not exist", principal));
        }
        UserInfoDetails userInfoDetails = new UserInfoDetails();
        userInfoDetails.setUserId(user.getId());
        userInfoDetails.setUserName(user.getUsername());
        userInfoDetails.setPhone(user.getMobile());
        userInfoDetails.setEmail(user.getEmail());
        userInfoDetails.setPassword(user.getPassword());
        userInfoDetails.setId(user.getId());
        return userInfoDetails;
    }

}
