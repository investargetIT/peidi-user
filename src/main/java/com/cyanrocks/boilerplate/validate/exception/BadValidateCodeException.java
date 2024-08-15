package com.cyanrocks.boilerplate.validate.exception;

import org.springframework.security.core.AuthenticationException;

public class BadValidateCodeException extends AuthenticationException {

    private static final long serialVersionUID = -7285211528095468156L;

    public BadValidateCodeException(String msg) {
        super(msg);
    }

}
