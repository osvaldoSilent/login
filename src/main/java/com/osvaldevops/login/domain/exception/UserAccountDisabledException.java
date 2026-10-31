package com.osvaldevops.login.domain.exception;

public class UserAccountDisabledException extends RuntimeException {

    public UserAccountDisabledException(String message) {
        super(message);
    }
}

