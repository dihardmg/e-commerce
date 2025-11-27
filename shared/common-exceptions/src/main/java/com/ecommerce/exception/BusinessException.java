package com.ecommerce.exception;

/**
 * Business Logic Exception
 *
 * Thrown when business rules are violated or business logic fails
 */
public class BusinessException extends ECommerceException {

    public BusinessException(String errorCode, String message) {
        super(errorCode, message);
    }

    public BusinessException(String errorCode, String message, Object... args) {
        super(errorCode, message, args);
    }

    public BusinessException(String errorCode, String serviceCode, String message) {
        super(errorCode, serviceCode, message);
    }

    public BusinessException(String errorCode, String serviceCode, String message, Object... args) {
        super(errorCode, serviceCode, message, args);
    }

    public BusinessException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public BusinessException(String errorCode, String serviceCode, String message, Throwable cause) {
        super(errorCode, serviceCode, message, cause);
    }
}