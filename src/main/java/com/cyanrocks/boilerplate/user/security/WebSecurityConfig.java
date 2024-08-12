package com.cyanrocks.boilerplate.user.security;

import com.tobee.brains.user.security.authentication.AuthProperties;
import com.tobee.brains.user.security.authentication.handler.DefaultExpiredSessionStrategy;
import com.tobee.brains.user.security.authentication.password.UsernamePasswordAuthSecurityConfig;
import com.tobee.brains.user.security.authentication.shopify.ShopifyOnlyAuthSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthProperties authProperties;

    @Autowired
    private UsernamePasswordAuthSecurityConfig usernamePasswordAuthSecurityConfig;

    @Autowired
    private ShopifyOnlyAuthSecurityConfig shopifyOnlyAuthSecurityConfig;

    @Autowired
    @Qualifier(value = "defaultLogoutSuccessHandler")
    private LogoutSuccessHandler logoutSuccessHandler;

    @Autowired
    private DefaultExpiredSessionStrategy defaultExpiredSessionStrategy;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.formLogin().loginPage(authProperties.getAuthRequireUrl()).and().apply(usernamePasswordAuthSecurityConfig)
            .and().apply(shopifyOnlyAuthSecurityConfig).and().authorizeRequests()
            .antMatchers(authProperties.getIgnoreAuthUrlsArray()).permitAll().anyRequest().authenticated().and()
            // 防止固定会话攻击，Spring security的默认配置就是如此：
            // 登陆成功之后会创建一个新的会话，然后将旧的session信息复制到新的session中（客户端的sessionId变了）
            .sessionManagement().sessionFixation().migrateSession()
            // .invalidSessionStrategy(defaultInvalidSessionStrategy)
            .maximumSessions(10).maxSessionsPreventsLogin(true).expiredSessionStrategy(defaultExpiredSessionStrategy)
            .and().and().logout().logoutUrl(authProperties.getLogoutUrl()).logoutSuccessHandler(logoutSuccessHandler)
            .deleteCookies("JSESSIONID")
            // .and()
            // .cors().configurationSource(corsConfigurationSource())
            .and()
            // .csrf().disable();
            // 开启csrf验证，需要前端同步传入token
            .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .ignoringAntMatchers(authProperties.getIgnoreCsrfUrlsArray());
    }

}
