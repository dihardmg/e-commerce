package com.ecommerce.monitoring.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * Logging Aspect
 *
 * Provides comprehensive logging for method execution
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private static final String LOGGING_PREFIX = "E-COMMERCE";

    /**
     * Log execution time and parameters for all public methods in service and controller classes
     */
    @Around("@within(org.springframework.web.bind.annotation.RestController) || " +
            "@within(org.springframework.stereotype.Service) || " +
            "@within(org.springframework.stereotype.Repository)")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName;

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            // Log method entry
            log.debug("[{}] Entering method: {} with arguments: {}",
                LOGGING_PREFIX, fullMethodName, Arrays.toString(joinPoint.getArgs()));

            Object result = joinPoint.proceed();

            stopWatch.stop();
            long executionTime = stopWatch.getLastTaskTimeMillis();

            // Log method exit for synchronous calls
            if (result instanceof CompletableFuture) {
                log.debug("[{}] Async method {} started with execution time: {}ms",
                    LOGGING_PREFIX, fullMethodName, executionTime);
            } else {
                log.debug("[{}] Exiting method: {} with result: {} and execution time: {}ms",
                    LOGGING_PREFIX, fullMethodName, result, executionTime);
            }

            return result;

        } catch (Throwable throwable) {
            stopWatch.stop();
            long executionTime = stopWatch.getLastTaskTimeMillis();

            log.error("[{}] Exception in method: {} with execution time: {}ms",
                LOGGING_PREFIX, fullMethodName, executionTime, throwable);

            throw throwable;
        }
    }

    /**
     * Log slow operations (methods that take longer than threshold)
     */
    @Around("@annotation(com.ecommerce.monitoring.annotation.LogSlowOperation)")
    public Object logSlowOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName;

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            if (executionTime > getSlowOperationThreshold(joinPoint)) {
                log.warn("[{}] SLOW OPERATION detected: {} took {}ms",
                    LOGGING_PREFIX, fullMethodName, executionTime);
            }

            return result;

        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("[{}] SLOW OPERATION failed: {} took {}ms",
                LOGGING_PREFIX, fullMethodName, executionTime, throwable);
            throw throwable;
        }
    }

    /**
     * Get slow operation threshold from annotation or use default
     */
    private long getSlowOperationThreshold(ProceedingJoinPoint joinPoint) {
        try {
            // Extract annotation from the method
            java.lang.reflect.Method method = ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature())
                    .getMethod();
            com.ecommerce.monitoring.annotation.LogSlowOperation annotation =
                    method.getAnnotation(com.ecommerce.monitoring.annotation.LogSlowOperation.class);

            if (annotation != null) {
                return annotation.thresholdMs();
            }
        } catch (NoSuchMethodException e) {
            log.debug("Could not extract method for annotation", e);
        }

        // Default threshold: 5 seconds
        return 5000L;
    }
}