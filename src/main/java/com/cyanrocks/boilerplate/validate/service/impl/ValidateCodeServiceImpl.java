package com.cyanrocks.boilerplate.validate.service.impl;


import com.cyanrocks.boilerplate.constants.ErrorCodeEnum;
import com.cyanrocks.boilerplate.constants.ValidateCodeTypeEnum;
import com.cyanrocks.boilerplate.exception.BusinessException;
import com.cyanrocks.boilerplate.validate.service.ValidateCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

;

/**
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
@Service
public class ValidateCodeServiceImpl implements ValidateCodeService {

    private static final Logger LOG = LoggerFactory.getLogger(ValidateCodeServiceImpl.class);

    private static final String PREFIX = "VALIDATE_CODE:";
    private static final String VALUE_HASH_KEY = "value";
    private static final String INSERT_TIME_HASH_KEY = "insertTime";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void checkCodeEffective(String identifier, String code, ValidateCodeTypeEnum validateCodeType) {
        // redis key的形式：VALIDATE_CODE:SMS_FORGET_PASSWORD#17625337619
        Object cachedCode
            = stringRedisTemplate.opsForHash().get(buildRedisKey(identifier, validateCodeType), VALUE_HASH_KEY);

        if (Objects.isNull(cachedCode)) {
            throw new BusinessException(ErrorCodeEnum.VALIDATE_CODE_NOT_EXIST_OR_EXPIRED.getCode(),
                String.format("验证码:%s不存在或者已过期", code));
        }
        if (!cachedCode.toString().equals(code)) {
            throw new BusinessException(ErrorCodeEnum.VALIDATE_CODE_NOT_MATCH.getCode(),
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
            case SMS: {
                generateSmsCodeAndSend(identifier, validateCodeType);
            }
            default: {
                throw new RuntimeException(String.format("不合法的发送通道:%s", validateCodeType.getChannel().name()));
            }
        }
    }

    @Override
    public void generateSmsCodeAndSend(String email, ValidateCodeTypeEnum validateCodeType) {

    }

    @Override
    public void generateEmailCodeAndSend(String email, ValidateCodeTypeEnum validateCodeType) {

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
