package com.ecommerce.exception;

/**
 * External Service Exception
 *
 * Thrown when communication with external services fails
 */
public class ExternalServiceException extends ECommerceException {

    private final String serviceName;
    private final int statusCode;

    public ExternalServiceException(String serviceName, String message) {
        super("EXTERNAL_SERVICE_ERROR", message);
        this.serviceName = serviceName;
        this.statusCode = -1;
    }

    public ExternalServiceException(String serviceName, int statusCode, String message) {
        super("EXTERNAL_SERVICE_ERROR", message);
        this.serviceName = serviceName;
        this.statusCode = statusCode;
    }

    public ExternalServiceException(String errorCode, String serviceName, String message) {
        super(errorCode, message);
        this.serviceName = serviceName;
        this.statusCode = -1;
    }

    public ExternalServiceException(String errorCode, String serviceName, int statusCode, String message) {
        super(errorCode, message);
        this.serviceName = serviceName;
        this.statusCode = statusCode;
    }

    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super("EXTERNAL_SERVICE_ERROR", message, cause);
        this.serviceName = serviceName;
        this.statusCode = -1;
    }

    public ExternalServiceException(String serviceName, int statusCode, String message, Throwable cause) {
        super("EXTERNAL_SERVICE_ERROR", message, cause);
        this.serviceName = serviceName;
        this.statusCode = statusCode;
    }

    /**
     * Create exception for connection timeout
     */
    public static ExternalServiceException connectionTimeout(String serviceName) {
        return new ExternalServiceException("SERVICE_CONNECTION_TIMEOUT", serviceName,
            String.format("Connection timeout occurred while communicating with %s", serviceName));
    }

    /**
     * Create exception for service unavailable
     */
    public static ExternalServiceException serviceUnavailable(String serviceName) {
        return new ExternalServiceException("SERVICE_UNAVAILABLE", serviceName,
            String.format("Service %s is currently unavailable", serviceName));
    }

    /**
     * Create exception for HTTP error response
     */
    public static ExternalServiceException httpError(String serviceName, int statusCode, String message) {
        return new ExternalServiceException("HTTP_ERROR", serviceName, statusCode,
            String.format("HTTP error %d from %s: %s", statusCode, serviceName, message));
    }

    /**
     * Create exception for invalid response format
     */
    public static ExternalServiceException invalidResponse(String serviceName) {
        return new ExternalServiceException("INVALID_RESPONSE", serviceName,
            String.format("Invalid response format from %s", serviceName));
    }

    /**
     * Create exception for rate limiting
     */
    public static ExternalServiceException rateLimited(String serviceName) {
        return new ExternalServiceException("RATE_LIMITED", serviceName,
            String.format("Rate limit exceeded for %s", serviceName));
    }
}