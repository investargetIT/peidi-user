package com.cyanrocks.boilerplate.validate.service;


import com.cyanrocks.boilerplate.constants.ValidateCodeTypeEnum;

public interface ValidateCodeGenerator {

    ValidateCode generate(ValidateCodeTypeEnum codeType);
}
