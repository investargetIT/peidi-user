package com.cyanrocks.boilerplate.user.validate.service;

import com.tobee.brains.user.constants.ValidateCodeTypeEnum;

public interface ValidateCodeGenerator {

    ValidateCode generate(ValidateCodeTypeEnum codeType);
}
