package com.ecommerce.exception;

/**
 * Authorization Exception
 *
 * Thrown when user is not authorized to perform an action
 */
public class AuthorizationException extends ECommerceException {

    public AuthorizationException(String message) {
        super("AUTHORIZATION_ERROR", message);
    }

    public AuthorizationException(String errorCode, String message) {
        super(errorCode, message);
    }

    public AuthorizationException(String resource, String action) {
        super("AUTHORIZATION_ERROR",
              String.format("User is not authorized to %s %s", action, resource));
    }

    public AuthorizationException(String resource, String action, String reason) {
        super("AUTHORIZATION_ERROR",
              String.format("User is not authorized to %s %s: %s", action, resource, reason));
    }

    public AuthorizationException(String message, Throwable cause) {
        super("AUTHORIZATION_ERROR", message, cause);
    }

    public AuthorizationException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}