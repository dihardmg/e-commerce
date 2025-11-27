package com.ecommerce.exception;

import com.ecommerce.dto.common.ErrorResponse;
import com.ecommerce.dto.common.ErrorDetail;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Base E-Commerce Exception Class
 */
@Getter
@Slf4j
public class ECommerceException extends RuntimeException {

    private final String errorCode;
    private final String serviceCode;
    private final Object[] args;
    private final List<ErrorDetail> errorDetails;
    private final LocalDateTime timestamp;

    public ECommerceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.serviceCode = null;
        this.args = null;
        this.errorDetails = null;
        this.timestamp = LocalDateTime.now();
    }

    public ECommerceException(String errorCode, String message, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.serviceCode = null;
        this.args = args;
        this.errorDetails = null;
        this.timestamp = LocalDateTime.now();
    }

    public ECommerceException(String errorCode, String serviceCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.serviceCode = serviceCode;
        this.args = null;
        this.errorDetails = null;
        this.timestamp = LocalDateTime.now();
    }

    public ECommerceException(String errorCode, String serviceCode, String message, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.serviceCode = serviceCode;
        this.args = args;
        this.errorDetails = null;
        this.timestamp = LocalDateTime.now();
    }

    public ECommerceException(String errorCode, String message, List<ErrorDetail> errorDetails) {
        super(message);
        this.errorCode = errorCode;
        this.serviceCode = null;
        this.args = null;
        this.errorDetails = errorDetails;
        this.timestamp = LocalDateTime.now();
    }

    public ECommerceException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.serviceCode = null;
        this.args = null;
        this.errorDetails = null;
        this.timestamp = LocalDateTime.now();
    }

    public ECommerceException(String errorCode, String serviceCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.serviceCode = serviceCode;
        this.args = null;
        this.errorDetails = null;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Convert exception to error response
     */
    public ErrorResponse toErrorResponse() {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .serviceCode(serviceCode)
                .message(getMessage())
                .errorDetails(errorDetails)
                .timestamp(timestamp)
                .build();
    }

    /**
     * Log the exception with appropriate log level
     */
    public void logException() {
        if (errorDetails != null && !errorDetails.isEmpty()) {
            log.error("E-Commerce Exception: {} - {} - Details: {}",
                errorCode, getMessage(), errorDetails, this);
        } else {
            log.error("E-Commerce Exception: {} - {}", errorCode, getMessage(), this);
        }
    }
}