package com.osvaldevops.login.domain.exception;

/**
 * Domain exception thrown when an authentication attempt is made on a disabled user account.
 */
public class UserAccountDisabledException extends RuntimeException {

    public UserAccountDisabledException(String message) {
        super(message);
    }
}
