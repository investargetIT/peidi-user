package com.cyanrocks.boilerplate.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @Author wjq
 * @Date 2024/8/16 9:43
 */
@Configuration
@PropertySource("classpath:sms.properties")
@ConfigurationProperties(prefix = "sms.aliyunsms")
@Component
public class SmsConfig {

    private String accessKeyId;
    private String accessKeySecret;
    private String endpoint;

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
