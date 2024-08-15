package com.cyanrocks.boilerplate.validate.service.impl;


import com.cyanrocks.boilerplate.constants.ValidateCodeTypeEnum;
import com.cyanrocks.boilerplate.validate.service.ValidateCode;
import com.cyanrocks.boilerplate.validate.service.ValidateCodeGenerator;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

/**
 * @Author lmh
 * @Date 2020/7/30 3:19 下午
 * @Version 1.0
 **/
@Component("defaultValidateCodeGenerator")
public class DefaultValidateCodeGenerator implements ValidateCodeGenerator {

    @Override
    public ValidateCode generate(ValidateCodeTypeEnum codeType) {
        String code = RandomStringUtils.randomNumeric(codeType.getLength());
        return new ValidateCode(code, codeType.getActiveTimeMs(), codeType);
    }
}
