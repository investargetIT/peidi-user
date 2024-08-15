package com.cyanrocks.boilerplate.security.authentication.password;

import com.cyanrocks.boilerplate.security.authentication.AuthProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

/**
 * 邮箱登陆的认证配置
 */
@Component
public class UsernamePasswordAuthSecurityConfig
    extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    @Autowired
    @Qualifier(value = "defaultLoginSuccessHandler")
    protected AuthenticationSuccessHandler defaultLoginSuccessHandler;

    @Autowired
    @Qualifier(value = "defaultLoginFailureHandler")
    protected AuthenticationFailureHandler defaultLoginFailureHandler;

    @Autowired
    @Qualifier(value = "passwordLoginUserDetailService")
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthProperties authProperties;

    @Override
    public void configure(HttpSecurity http) throws Exception {

        UsernamePasswordAuthFilter usernamePasswordAuthFilter
            = new UsernamePasswordAuthFilter(authProperties.getPasswordLoginUrl());
        usernamePasswordAuthFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
        usernamePasswordAuthFilter.setAuthenticationSuccessHandler(defaultLoginSuccessHandler);
        usernamePasswordAuthFilter.setAuthenticationFailureHandler(defaultLoginFailureHandler);

        UsernamePasswordAuthProvider emailAuthenticationProvider = new UsernamePasswordAuthProvider();
        emailAuthenticationProvider.setUserDetailsService(userDetailsService);
        // 确保对密码进行加密的encoder和解密的encoder相同
        emailAuthenticationProvider.setPasswordEncoder(passwordEncoder);

        http.authenticationProvider(emailAuthenticationProvider).addFilterAfter(usernamePasswordAuthFilter,
            UsernamePasswordAuthenticationFilter.class);

    }

}
