package com.cyanrocks.boilerplate.user.validate.service.impl;

import com.tobee.brains.common.exception.TobeeBrainsBusinessException;
import com.tobee.brains.common.integration.aliemail.EmailParam;
import com.tobee.brains.common.integration.aliemail.EmailSender;
import com.tobee.brains.user.constants.ValidateCodeTypeEnum;
import com.tobee.brains.user.validate.errorcode.UserErrorCodeEnum;
import com.tobee.brains.user.validate.service.ValidateCode;
import com.tobee.brains.user.validate.service.ValidateCodeGenerator;
import com.tobee.brains.user.validate.service.ValidateCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

;

/**
 * @Author lmh
 * @Date 2020/7/30 2:08 下午
 * @Version 1.0
 **/
@Service
public class ValidateCodeServiceImpl implements ValidateCodeService {

    private static final Logger LOG = LoggerFactory.getLogger(ValidateCodeServiceImpl.class);

    private static final String PREFIX = "VALIDATE_CODE:";
    private static final String VALUE_HASH_KEY = "value";
    private static final String INSERT_TIME_HASH_KEY = "insertTime";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    @Qualifier(value = "defaultValidateCodeGenerator")
    private ValidateCodeGenerator validateCodeGenerator;

    @Override
    public void checkCodeEffective(String identifier, String code, ValidateCodeTypeEnum validateCodeType) {
        // redis key的形式：VALIDATE_CODE:SMS_FORGET_PASSWORD#17625337619
        Object cachedCode
            = stringRedisTemplate.opsForHash().get(buildRedisKey(identifier, validateCodeType), VALUE_HASH_KEY);

        if (Objects.isNull(cachedCode)) {
            throw new TobeeBrainsBusinessException(UserErrorCodeEnum.VALIDATE_CODE_NOT_EXIST_OR_EXPIRED.getCode(),
                String.format("验证码:%s不存在或者已过期", code));
        }
        if (!cachedCode.toString().equals(code)) {
            throw new TobeeBrainsBusinessException(UserErrorCodeEnum.VALIDATE_CODE_NOT_MATCH.getCode(),
                String.format("验证码%s错误", code));
        }
    }

    @Override
    public void generateValidateCodeAndSend(String identifier, ValidateCodeTypeEnum validateCodeType) {
        switch (validateCodeType.getChannel()) {

            case EMAIL: {
                generateEmailCodeAndSend(identifier, validateCodeType);
                break;
            }
            default: {
                throw new RuntimeException(String.format("不合法的发送通道:%s", validateCodeType.getChannel().name()));
            }
        }
    }

    @Override
    public void generateEmailCodeAndSend(String email, ValidateCodeTypeEnum validateCodeType) {
        String cacheKey = buildRedisKey(email, validateCodeType);
        ValidateCode code = validateCodeGenerator.generate(validateCodeType);
        if (isCodeSentTooOften(cacheKey)) {
            throw new TobeeBrainsBusinessException(UserErrorCodeEnum.VALIDATE_CODE_SENT_TOO_OFTEN.getCode(),
                String.format("%s 验证码发送太频繁，请稍后再试", email));
        }
        // 构建hash结构，存储验证码的值以及验证码的设置时间
        stringRedisTemplate.opsForHash().put(cacheKey, VALUE_HASH_KEY, code.getCode());
        stringRedisTemplate.opsForHash().put(cacheKey, INSERT_TIME_HASH_KEY,
            String.valueOf(System.currentTimeMillis()));
        sendEmail(email, code.getCode(), validateCodeType);
    }

    private void sendEmail(String toAddress, String code, ValidateCodeTypeEnum validateCodeType) {
        switch (validateCodeType) {
            case EMAIL_REGISTER: {
                Context context = new Context();
                context.setVariable("validateCode", code);
                String mailContent = templateEngine.process("sign_up", context);
                EmailParam param = new EmailParam();
                param.setSubject("Email Sign Up");
                param.setSendDate(new Date());
                param.setDestination(toAddress);
                param.setBody(mailContent);
                emailSender.sendHtmlAsync(param);
                break;
            }
            case EMAIL_RESET_PASSWORD: {
                Context context = new Context();
                context.setVariable("validateCode", code);
                context.setVariable("email", toAddress);
                String mailContent = templateEngine.process("reset_password", context);
                EmailParam param = new EmailParam();
                param.setSubject("Reset Password");
                param.setSendDate(new Date());
                param.setDestination(toAddress);
                param.setBody(mailContent);
                emailSender.sendHtmlAsync(param);
                break;
            }
        }
    }

    private boolean isCodeSentTooOften(String cacheKey) {
        Object insertTime = stringRedisTemplate.opsForHash().get(cacheKey, INSERT_TIME_HASH_KEY);
        if (Objects.isNull(insertTime)) {
            return false;
        }
        long timeSinceLastSent = System.currentTimeMillis() - Long.parseLong((String)insertTime);
        return timeSinceLastSent <= TimeUnit.SECONDS.toMillis(25);
    }

    private String buildRedisKey(String destination, ValidateCodeTypeEnum validateCodeTypeEnum) {
        return PREFIX + validateCodeTypeEnum.name() + "#" + destination;
    }
}
