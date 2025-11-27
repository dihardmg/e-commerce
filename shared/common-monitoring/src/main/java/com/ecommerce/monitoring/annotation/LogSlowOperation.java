package com.ecommerce.monitoring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark slow operations for logging
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogSlowOperation {

    /**
     * Threshold in milliseconds after which operation is considered slow
     */
    long thresholdMs() default 5000;

    /**
     * Custom log message template
     */
    String message() default "Operation exceeded threshold";
}