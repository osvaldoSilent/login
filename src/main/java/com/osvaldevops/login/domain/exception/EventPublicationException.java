package com.osvaldevops.login.domain.exception;

/**
 * Domain exception thrown when publishing a domain event to the messaging broker fails.
 */
public class EventPublicationException extends RuntimeException {

    public EventPublicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
