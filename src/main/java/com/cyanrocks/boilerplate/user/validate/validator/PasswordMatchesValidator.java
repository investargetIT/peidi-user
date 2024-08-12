package com.cyanrocks.boilerplate.user.validate.validator;

import com.tobee.brains.user.model.request.RegistrationCommand;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public void initialize(final PasswordMatches constraintAnnotation) {
        //
    }

    @Override
    public boolean isValid(final Object obj, final ConstraintValidatorContext context) {
        final RegistrationCommand registrationCommand = (RegistrationCommand)obj;
        return registrationCommand.getPassword().equals(registrationCommand.getMatchingPassword());
    }

}
