package com.cyanrocks.boilerplate.user.security.authentication.password;

import com.tobee.brains.user.security.authentication.assembler.UserInfoDetailsAssembler;
import com.tobee.brainservice.share.common.model.Result;
import com.tobee.brainservice.share.user.model.dto.UserInfoDTO;
import com.tobee.brainservice.share.user.service.UserRemoteService;
import org.apache.dubbo.config.annotation.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Reference(version = "1.0.0")
    private UserRemoteService userRemoteService;

    @Override
    public UserDetails loadUserByUsername(String username) throws AuthenticationException {
        if (logger.isInfoEnabled()) {
            logger.info("用户名密码方式登陆, 用户名={}", username);
        }
        Result<UserInfoDTO> result = userRemoteService.findCompleteUserInfoByIdentifierInPasswordAuth(username);
        if (!result.isSuccess()) {
            throw new InternalAuthenticationServiceException(
                "get user error,username=" + username + ",error=" + result.getError());
        }
        // 根据用户名查找用户信息
        if (result.getData() == null) {
            throw new UsernameNotFoundException(String.format("%s user not exist", username));
        }
        return UserInfoDetailsAssembler.buildUserDetails(result.getData());
    }
}
