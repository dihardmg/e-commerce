package com.ecommerce.monitoring.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Metrics Service
 *
 * Provides centralized metrics collection
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;

    /**
     * Record a counter metric
     */
    public void incrementCounter(String metricName, String... tags) {
        try {
            Counter.builder(metricName)
                    .tags(tags)
                    .register(meterRegistry)
                    .increment();
        } catch (Exception e) {
            log.warn("Failed to increment counter metric: {}", metricName, e);
        }
    }

    /**
     * Record a counter with value
     */
    public void incrementCounter(String metricName, double amount, String... tags) {
        try {
            Counter.builder(metricName)
                    .tags(tags)
                    .register(meterRegistry)
                    .increment(amount);
        } catch (Exception e) {
            log.warn("Failed to increment counter metric: {} by amount: {}", metricName, amount, e);
        }
    }

    /**
     * Record a timer metric
     */
    public void recordTimer(String metricName, long duration, TimeUnit timeUnit, String... tags) {
        try {
            Timer.builder(metricName)
                    .tags(tags)
                    .register(meterRegistry)
                    .record(duration, timeUnit);
        } catch (Exception e) {
            log.warn("Failed to record timer metric: {} with duration: {}", metricName, duration, e);
        }
    }

    /**
     * Record a timer metric using Duration
     */
    public void recordTimer(String metricName, Duration duration, String... tags) {
        try {
            Timer.builder(metricName)
                    .tags(tags)
                    .register(meterRegistry)
                    .record(duration);
        } catch (Exception e) {
            log.warn("Failed to record timer metric: {} with duration: {}", metricName, duration, e);
        }
    }

    /**
     * Time and record an operation
     */
    public <T> T timeAndRecord(String metricName, Supplier<T> operation, String... tags) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            T result = operation.get();
            sample.stop(Timer.builder(metricName).tags(tags).register(meterRegistry));
            return result;
        } catch (Exception e) {
            sample.stop(Timer.builder(metricName).tags(tags).tag("result", "error").register(meterRegistry));
            throw e;
        }
    }

    /**
     * Record a gauge metric
     */
    public <T extends Number> void recordGauge(String metricName, T value, String... tags) {
        try {
            Gauge.builder(metricName, value, Number::doubleValue)
                    .tags(tags)
                    .register(meterRegistry);
        } catch (Exception e) {
            log.warn("Failed to record gauge metric: {} with value: {}", metricName, value, e);
        }
    }

    /**
     * Record a gauge with supplier
     */
    public <T> void recordGauge(String metricName, Supplier<T> supplier, String... tags) {
        try {
            Gauge.builder(metricName, supplier, s -> {
                T value = s.get();
                return value instanceof Number ? ((Number) value).doubleValue() : 0.0;
            })
                    .tags(tags)
                    .register(meterRegistry);
        } catch (Exception e) {
            log.warn("Failed to record gauge metric: {}", metricName, e);
        }
    }

    /**
     * Increment HTTP request counter
     */
    public void recordHttpRequest(String method, String endpoint, int statusCode, long durationMs) {
        try {
            Counter.builder("http.requests.total")
                    .tag("method", method)
                    .tag("endpoint", endpoint)
                    .tag("status", String.valueOf(statusCode))
                    .tag("status_family", getStatusFamily(statusCode))
                    .register(meterRegistry)
                    .increment();

            Timer.builder("http.request.duration")
                    .tag("method", method)
                    .tag("endpoint", endpoint)
                    .tag("status", String.valueOf(statusCode))
                    .register(meterRegistry)
                    .record(durationMs, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            log.warn("Failed to record HTTP request metric", e);
        }
    }

    /**
     * Increment business event counter
     */
    public void recordBusinessEvent(String eventType, String sourceService, boolean success) {
        try {
            Counter.builder("business.events.total")
                    .tag("event_type", eventType)
                    .tag("source_service", sourceService)
                    .tag("result", success ? "success" : "error")
                    .register(meterRegistry)
                    .increment();

        } catch (Exception e) {
            log.warn("Failed to record business event metric", e);
        }
    }

    /**
     * Record database operation metrics
     */
    public void recordDatabaseOperation(String operation, String table, boolean success, long durationMs) {
        try {
            Counter.builder("database.operations.total")
                    .tag("operation", operation)
                    .tag("table", table)
                    .tag("result", success ? "success" : "error")
                    .register(meterRegistry)
                    .increment();

            Timer.builder("database.operation.duration")
                    .tag("operation", operation)
                    .tag("table", table)
                    .register(meterRegistry)
                    .record(durationMs, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            log.warn("Failed to record database operation metric", e);
        }
    }

    /**
     * Record external service call metrics
     */
    public void recordExternalServiceCall(String serviceName, String operation, boolean success, long durationMs) {
        try {
            Counter.builder("external.service.calls.total")
                    .tag("service", serviceName)
                    .tag("operation", operation)
                    .tag("result", success ? "success" : "error")
                    .register(meterRegistry)
                    .increment();

            Timer.builder("external.service.call.duration")
                    .tag("service", serviceName)
                    .tag("operation", operation)
                    .register(meterRegistry)
                    .record(durationMs, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            log.warn("Failed to record external service call metric", e);
        }
    }

    /**
     * Record cache hit/miss metrics
     */
    public void recordCacheOperation(String cacheName, String operation, boolean hit) {
        try {
            Counter.builder("cache.operations.total")
                    .tag("cache", cacheName)
                    .tag("operation", operation)
                    .tag("result", hit ? "hit" : "miss")
                    .register(meterRegistry)
                    .increment();

        } catch (Exception e) {
            log.warn("Failed to record cache operation metric", e);
        }
    }

    /**
     * Get status family for HTTP status codes
     */
    private String getStatusFamily(int statusCode) {
        if (statusCode >= 200 && statusCode < 300) return "2xx";
        if (statusCode >= 300 && statusCode < 400) return "3xx";
        if (statusCode >= 400 && statusCode < 500) return "4xx";
        if (statusCode >= 500) return "5xx";
        return "unknown";
    }
}