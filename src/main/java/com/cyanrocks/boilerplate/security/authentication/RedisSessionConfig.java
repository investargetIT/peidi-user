package com.cyanrocks.boilerplate.security.authentication;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
@Configuration
// 设置session的过期时间为一天
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 24*60*60)
public class RedisSessionConfig {

    @Bean
    public CookieHttpSessionIdResolver sessionIdResolver() {
        // 创建 CookieHttpSessionIdResolver 对象
        CookieHttpSessionIdResolver sessionIdResolver = new CookieHttpSessionIdResolver();

        // 创建 DefaultCookieSerializer 对象
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        sessionIdResolver.setCookieSerializer(cookieSerializer); // 设置到 sessionIdResolver 中
        cookieSerializer.setCookieName("JSESSIONID");

        return sessionIdResolver;
    }
}
