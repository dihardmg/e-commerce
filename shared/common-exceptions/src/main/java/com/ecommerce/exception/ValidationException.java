package com.ecommerce.exception;

import com.ecommerce.dto.common.ErrorDetail;
import jakarta.validation.ConstraintViolation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Validation Exception
 *
 * Thrown when validation fails
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationException extends ECommerceException {

    public ValidationException(List<ErrorDetail> errorDetails) {
        super("VALIDATION_ERROR", "Validation failed", errorDetails);
    }

    public ValidationException(String errorCode, List<ErrorDetail> errorDetails) {
        super(errorCode, "Validation failed", errorDetails);
    }

    public ValidationException(String errorCode, String message, List<ErrorDetail> errorDetails) {
        super(errorCode, message, errorDetails);
    }

    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }

    public ValidationException(String errorCode, String message) {
        super(errorCode, message);
    }

    public ValidationException(String message, Object... args) {
        super("VALIDATION_ERROR", message, args);
    }

    public ValidationException(String errorCode, String message, Object... args) {
        super(errorCode, message, args);
    }

    public ValidationException(String message, Throwable cause) {
        super("VALIDATION_ERROR", message, cause);
    }

    public ValidationException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    /**
     * Create validation exception from constraint violations
     */
    public static <T> ValidationException fromViolations(
            java.util.Set<ConstraintViolation<T>> violations) {
        List<ErrorDetail> errorDetails = violations.stream()
                .map(violation -> ErrorDetail.builder()
                        .field(violation.getPropertyPath().toString())
                        .message(violation.getMessage())
                        .rejectedValue(violation.getInvalidValue())
                        .build())
                .collect(Collectors.toList());

        return new ValidationException("CONSTRAINT_VIOLATION", "Validation constraints violated", errorDetails);
    }

    /**
     * Create validation exception for field-specific errors
     */
    public static ValidationException fieldError(String field, String message, Object rejectedValue) {
        ErrorDetail errorDetail = ErrorDetail.builder()
                .field(field)
                .message(message)
                .rejectedValue(rejectedValue)
                .build();

        return new ValidationException(List.of(errorDetail));
    }

    /**
     * Create validation exception for multiple field errors
     */
    public static ValidationException fieldErrors(List<ErrorDetail> errorDetails) {
        return new ValidationException("FIELD_VALIDATION_ERROR", "Field validation failed", errorDetails);
    }

    /**
     * Create validation exception for business rule violations
     */
    public static ValidationException businessRuleViolation(String ruleCode, String message, Object... args) {
        return new ValidationException(ruleCode, String.format(message, args));
    }

    /**
     * Create validation exception for required field
     */
    public static ValidationException requiredField(String fieldName) {
        return fieldError(fieldName, String.format("%s is required", fieldName), null);
    }

    /**
     * Create validation exception for invalid format
     */
    public static ValidationException invalidFormat(String fieldName, String expectedFormat, Object actualValue) {
        return fieldError(fieldName,
                String.format("Invalid format for %s. Expected: %s", fieldName, expectedFormat),
                actualValue);
    }

    /**
     * Create validation exception for invalid length
     */
    public static ValidationException invalidLength(String fieldName, int minLength, int maxLength, Object actualValue) {
        String message = String.format("%s length must be between %d and %d", fieldName, minLength, maxLength);
        return fieldError(fieldName, message, actualValue);
    }

    /**
     * Create validation exception for value out of range
     */
    public static ValidationException outOfRange(String fieldName, Number min, Number max, Object actualValue) {
        String message = String.format("%s must be between %s and %s", fieldName, min, max);
        return fieldError(fieldName, message, actualValue);
    }
}