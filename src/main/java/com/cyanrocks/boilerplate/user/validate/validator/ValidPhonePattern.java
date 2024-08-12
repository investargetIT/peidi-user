package com.cyanrocks.boilerplate.user.validate.validator;

/**
 * @Author lmh
 * @Date 2020/7/30 7:48 下午
 * @Version 1.0
 **/

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {PhoneValidator.class})
public @interface ValidPhonePattern {

    String regexp() default "";

    String message() default "Invalid Phone No Pattern";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
