package com.cyanrocks.boilerplate.user.constants;

import java.util.concurrent.TimeUnit;

/**
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
public enum ValidateCodeTypeEnum {
    /**
     * 短信注册验证码
     */
    SMS_REGISTER("sms_register", TimeUnit.MINUTES.toMillis(15), 6, Channel.SMS),

    /**
     * 邮箱注册验证码
     */
    EMAIL_REGISTER("email_register", TimeUnit.MINUTES.toMillis(15), 6, Channel.EMAIL),

    /**
     * 短信登陆验证码
     */
    SMS_LOGIN("sms_login", TimeUnit.MINUTES.toMillis(15), 6, Channel.SMS),

    /**
     * 邮箱+验证码的登陆方式
     */
    EMAIL_CODE_LOGIN("email_code_login", TimeUnit.MINUTES.toMillis(15), 6, Channel.EMAIL),

    /**
     * 忘记密码后 邮箱修改密码的验证码
     */
    EMAIL_RESET_PASSWORD("email_reset_password", TimeUnit.MINUTES.toMillis(15), 6, Channel.EMAIL),

    /**
     * 忘记密码后 短信修改密码的验证码
     */
    SMS_RESET_PASSWORD("sms_reset_password", TimeUnit.MINUTES.toMillis(15), 6, Channel.SMS);

    private String key;

    /**
     * 验证码的存储时间
     */
    long activeTimeMs;

    /**
     * 验证码的长度
     */
    int length;

    /**
     * 发送的通道
     */
    Channel channel;

    ValidateCodeTypeEnum(String key, long activeTimeMs, int length, Channel channel) {
        this.key = key;
        this.activeTimeMs = activeTimeMs;
        this.length = length;
        this.channel = channel;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getActiveTimeMs() {
        return activeTimeMs;
    }

    public void setActiveTimeMs(long activeTimeMs) {
        this.activeTimeMs = activeTimeMs;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public static ValidateCodeTypeEnum of(String key) {
        for (ValidateCodeTypeEnum value : ValidateCodeTypeEnum.values()) {
            if (key.equals(value.getKey())) {
                return value;
            }
        }
        throw new RuntimeException(String.format("不合法的ValidateCodeTypeEnum = %s", key));
    }

    public enum Channel {
        SMS, EMAIL
    }
}
