package com.cyanrocks.boilerplate.user.validate.service;

import com.tobee.brains.user.constants.ValidateCodeTypeEnum;

public interface ValidateCodeService {

    void checkCodeEffective(String destination, String code, ValidateCodeTypeEnum validateCodeType);

    void generateValidateCodeAndSend(String destination, ValidateCodeTypeEnum validateCodeType);

    void generateEmailCodeAndSend(String email, ValidateCodeTypeEnum validateCodeType);
}
