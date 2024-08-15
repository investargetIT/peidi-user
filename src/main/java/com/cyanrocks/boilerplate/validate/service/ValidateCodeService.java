package com.cyanrocks.boilerplate.validate.service;


import com.cyanrocks.boilerplate.constants.ValidateCodeTypeEnum;

public interface ValidateCodeService {

    void checkCodeEffective(String destination, String code, ValidateCodeTypeEnum validateCodeType);

    void generateValidateCodeAndSend(String destination, ValidateCodeTypeEnum validateCodeType);

    void generateEmailCodeAndSend(String email, ValidateCodeTypeEnum validateCodeType);

    void generateSmsCodeAndSend(String email, ValidateCodeTypeEnum validateCodeType);
}
