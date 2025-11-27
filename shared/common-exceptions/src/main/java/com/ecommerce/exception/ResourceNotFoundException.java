package com.ecommerce.exception;

/**
 * Resource Not Found Exception
 *
 * Thrown when requested resource is not found
 */
public class ResourceNotFoundException extends ECommerceException {

    public ResourceNotFoundException(String resource, String identifier) {
        super("RESOURCE_NOT_FOUND", String.format("%s with identifier '%s' not found", resource, identifier));
    }

    public ResourceNotFoundException(String errorCode, String resource, String identifier) {
        super(errorCode, String.format("%s with identifier '%s' not found", resource, identifier));
    }

    public ResourceNotFoundException(String resource, Object identifier) {
        super("RESOURCE_NOT_FOUND", String.format("%s with identifier '%s' not found", resource, identifier));
    }

    public ResourceNotFoundException(String errorCode, String resource, Object identifier) {
        super(errorCode, String.format("%s with identifier '%s' not found", resource, identifier));
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        super("RESOURCE_NOT_FOUND",
              String.format("%s with %s '%s' not found", resource, field, value));
    }

    public ResourceNotFoundException(String errorCode, String resource, String field, Object value) {
        super(errorCode,
              String.format("%s with %s '%s' not found", resource, field, value));
    }

    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message);
    }

    public ResourceNotFoundException(String errorCode, String message) {
        super(errorCode, message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super("RESOURCE_NOT_FOUND", message, cause);
    }

    public ResourceNotFoundException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}