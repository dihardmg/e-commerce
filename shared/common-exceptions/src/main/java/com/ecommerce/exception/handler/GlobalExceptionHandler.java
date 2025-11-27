package com.ecommerce.exception.handler;

import com.ecommerce.dto.common.ErrorResponse;
import com.ecommerce.dto.common.ErrorDetail;
import com.ecommerce.dto.common.CommonResponse;
import com.ecommerce.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global Exception Handler
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    /**
     * Handle E-Commerce exceptions
     */
    @ExceptionHandler(ECommerceException.class)
    public ResponseEntity<ErrorResponse> handleECommerceException(
            ECommerceException ex, HttpServletRequest request) {

        ex.logException();

        ErrorResponse errorResponse = ex.toErrorResponse();
        errorResponse.setPath(request.getRequestURI());

        HttpStatus status = determineHttpStatus(ex.getErrorCode());

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Handle validation exceptions from @Valid
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ErrorDetail> errorDetails = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::createErrorDetail)
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("VALIDATION_ERROR")
                .message("Validation failed")
                .errorDetails(errorDetails)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        log.warn("Validation error: {}", errorDetails);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle constraint violations
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        List<ErrorDetail> errorDetails = ex.getConstraintViolations()
                .stream()
                .map(violation -> ErrorDetail.builder()
                        .field(violation.getPropertyPath().toString())
                        .message(violation.getMessage())
                        .rejectedValue(violation.getInvalidValue())
                        .build())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("CONSTRAINT_VIOLATION")
                .message("Constraint validation failed")
                .errorDetails(errorDetails)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        log.warn("Constraint violation: {}", errorDetails);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle authentication exceptions
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("AUTHENTICATION_ERROR")
                .message("Authentication failed: " + ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        log.warn("Authentication error: {}", ex.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle authorization exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("ACCESS_DENIED")
                .message("Access denied: " + ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        log.warn("Access denied: {}", ex.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle resource not found exceptions
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(
            NoHandlerFoundException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("ENDPOINT_NOT_FOUND")
                .message("The requested endpoint was not found")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        log.warn("Endpoint not found: {}", ex.getRequestURL());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle method not supported exceptions
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("METHOD_NOT_SUPPORTED")
                .message("HTTP method '" + ex.getMethod() + "' is not supported for this endpoint")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        log.warn("Method not supported: {} {}", ex.getMethod(), ex.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Handle argument type mismatch
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String message = String.format("Parameter '%s' should be of type %s",
            ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("TYPE_MISMATCH")
                .message(message)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        log.warn("Type mismatch: {}", message);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle malformed JSON
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonMalformed(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("JSON_PARSE_ERROR")
                .message("Malformed JSON request")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        log.warn("JSON parse error: {}", ex.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        log.error("Unexpected error occurred", ex);

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Create error detail from field error
     */
    private ErrorDetail createErrorDetail(FieldError fieldError) {
        String message = fieldError.getDefaultMessage();
        if (message != null) {
            try {
                message = messageSource.getMessage(message, null, message, LocaleContextHolder.getLocale());
            } catch (Exception e) {
                log.debug("Could not resolve message: {}", message);
            }
        }

        return ErrorDetail.builder()
                .field(fieldError.getField())
                .message(message)
                .rejectedValue(fieldError.getRejectedValue())
                .build();
    }

    /**
     * Determine HTTP status based on error code
     */
    private HttpStatus determineHttpStatus(String errorCode) {
        return switch (errorCode) {
            case "VALIDATION_ERROR", "CONSTRAINT_VIOLATION", "TYPE_MISMATCH",
                 "JSON_PARSE_ERROR", "FIELD_VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST;
            case "AUTHENTICATION_ERROR" -> HttpStatus.UNAUTHORIZED;
            case "AUTHORIZATION_ERROR", "ACCESS_DENIED" -> HttpStatus.FORBIDDEN;
            case "RESOURCE_NOT_FOUND", "ENDPOINT_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "METHOD_NOT_SUPPORTED" -> HttpStatus.METHOD_NOT_ALLOWED;
            case "EXTERNAL_SERVICE_ERROR", "SERVICE_UNAVAILABLE",
                 "SERVICE_CONNECTION_TIMEOUT" -> HttpStatus.SERVICE_UNAVAILABLE;
            case "RATE_LIMITED" -> HttpStatus.TOO_MANY_REQUESTS;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}