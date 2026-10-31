package com.osvaldevops.login.domain.exception;

/**
 * Domain exception thrown when attempting to register a user with an email address that already exists.
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
