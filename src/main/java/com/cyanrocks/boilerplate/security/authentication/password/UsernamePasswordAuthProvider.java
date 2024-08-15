package com.cyanrocks.boilerplate.security.authentication.password;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
public class UsernamePasswordAuthProvider implements AuthenticationProvider {

    private UserDetailsService userDetailsService;

    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof UsernamePasswordAuthToken)) {
            throw new RuntimeException("CustomAuthProvider 只支持 CustomAuthToken");
        }
        UsernamePasswordAuthToken authenticationToken = (UsernamePasswordAuthToken)authentication;
        UserDetails user = userDetailsService.loadUserByUsername((String)authenticationToken.getPrincipal());

        if (user == null) {
            throw new InternalAuthenticationServiceException("无法获取用户信息");
        }
        additionalAuthenticationChecks(user, authenticationToken);
        // 将用户定义的user放入token中，这样可以在session中查询到所有自定义的用户信息
        return new UsernamePasswordAuthToken(user, user.getPassword(), user.getAuthorities());
    }

    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthToken authentication)
        throws AuthenticationException {
        if (authentication.getCredentials() == null) {
            throw new BadCredentialsException("认证失败，密码为空");
        }

        String presentedPassword = authentication.getCredentials().toString();

        if (!passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {
            throw new BadCredentialsException("账号密码不匹配!");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthToken.class.isAssignableFrom(authentication);
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

}
