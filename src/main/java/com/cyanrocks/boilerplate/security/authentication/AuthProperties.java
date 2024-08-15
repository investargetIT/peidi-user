package com.cyanrocks.boilerplate.security.authentication;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
@Configuration
@PropertySource("classpath:security/auth.properties")
@ConfigurationProperties(prefix = "security.auth")
@Component
public class AuthProperties {

    private String authRequireUrl;

    private String passwordLoginUrl;

    private String shopifyLoginUrl;

    private String logoutUrl;

    private List<String> ignoreAuthUrls;

    private List<String> ignoreCsrfUrls;

    public String getAuthRequireUrl() {
        return authRequireUrl;
    }

    public void setAuthRequireUrl(String authRequireUrl) {
        this.authRequireUrl = authRequireUrl;
    }

    public String getPasswordLoginUrl() {
        return passwordLoginUrl;
    }

    public void setPasswordLoginUrl(String passwordLoginUrl) {
        this.passwordLoginUrl = passwordLoginUrl;
    }

    public List<String> getIgnoreAuthUrls() {
        return ignoreAuthUrls;
    }

    public String[] getIgnoreAuthUrlsArray() {
        String[] stringArray = new String[ignoreAuthUrls.size()];
        return ignoreAuthUrls.toArray(stringArray);
    }

    public void setIgnoreAuthUrls(List<String> ignoreAuthUrls) {
        this.ignoreAuthUrls = ignoreAuthUrls;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }

    public List<String> getIgnoreCsrfUrls() {
        return ignoreCsrfUrls;
    }

    public String[] getIgnoreCsrfUrlsArray() {
        String[] stringArray = new String[ignoreCsrfUrls.size()];
        return ignoreCsrfUrls.toArray(stringArray);
    }

    public void setIgnoreCsrfUrls(List<String> ignoreCsrfUrls) {
        this.ignoreCsrfUrls = ignoreCsrfUrls;
    }

    public String getShopifyLoginUrl() {
        return shopifyLoginUrl;
    }

    public void setShopifyLoginUrl(String shopifyLoginUrl) {
        this.shopifyLoginUrl = shopifyLoginUrl;
    }
}
