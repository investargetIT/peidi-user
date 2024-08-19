package com.cyanrocks.boilerplate.validate.service.impl;


import com.cyanrocks.boilerplate.constants.ValidateCodeTypeEnum;
import com.cyanrocks.boilerplate.validate.service.ValidateCode;
import com.cyanrocks.boilerplate.validate.service.ValidateCodeGenerator;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

/**
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
@Component("defaultValidateCodeGenerator")
public class DefaultValidateCodeGenerator implements ValidateCodeGenerator {

    @Override
    public ValidateCode generate(ValidateCodeTypeEnum codeType) {
        String code = RandomStringUtils.randomNumeric(codeType.getLength());
        return new ValidateCode(code, codeType.getActiveTimeMs(), codeType);
    }
}
