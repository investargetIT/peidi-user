package com.cyanrocks.boilerplate.validate.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author lmh
 * @Date 2020/7/30 7:50 下午
 * @Version 1.0
 **/
public class PhoneValidator implements ConstraintValidator<ValidPhonePattern, String> {

    @Override
    public void initialize(final ValidPhonePattern arg0) {

    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        Pattern p
            = Pattern.compile("^((13[0-9])|(14[5,7])|(15[^4,\\D])|(17[0,1,3,6-8])|(18[0-9])|(19[8,9])|(166))[0-9]{8}$");
        Matcher m = p.matcher(value);
        return m.matches();

    }
}
