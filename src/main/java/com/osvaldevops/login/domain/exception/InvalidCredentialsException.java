package com.osvaldevops.login.domain.exception;

/**
 * Domain exception thrown when user authentication fails due to an unknown email or mismatched password.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
