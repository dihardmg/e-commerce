package com.ecommerce.monitoring.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Health Check Service
 *
 * Provides comprehensive health checks for various components
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthCheckService {

    private final Map<String, HealthCheckResult> healthCheckCache = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<HealthCheckResult>> healthCheckFutures = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    private final MetricsService metricsService;

    /**
     * Health Check Result
     */
    @Data
    public static class HealthCheckResult {
        private boolean healthy;
        private String message;
        private LocalDateTime timestamp;
        private Map<String, Object> details;

        public HealthCheckResult(boolean healthy, String message) {
            this.healthy = healthy;
            this.message = message;
            this.timestamp = LocalDateTime.now();
            this.details = new ConcurrentHashMap<>();
        }

        public static HealthCheckResult healthy(String message) {
            return new HealthCheckResult(true, message);
        }

        public static HealthCheckResult unhealthy(String message) {
            return new HealthCheckResult(false, message);
        }

        public HealthCheckResult withDetail(String key, Object value) {
            this.details.put(key, value);
            return this;
        }
    }

    /**
     * Check database health
     */
    public HealthCheckResult checkDatabaseHealth() {
        return healthCheckFutures.computeIfAbsent("database", key ->
            CompletableFuture.supplyAsync(() -> {
                try {
                    if (jdbcTemplate != null) {
                        String query = jdbcTemplate.queryForObject(
                                "SELECT 1", String.class);

                        return HealthCheckResult.healthy("Database connection is healthy")
                                .withDetail("query_result", query);
                    } else {
                        return HealthCheckResult.healthy("Database not configured");
                    }
                } catch (Exception e) {
                    log.error("Database health check failed", e);
                    metricsService.incrementCounter("health.check.database", "result", "error");
                    return HealthCheckResult.unhealthy("Database connection failed: " + e.getMessage())
                            .withDetail("error", e.getClass().getSimpleName());
                }
            })
        ).join();
    }

    /**
     * Check Redis health
     */
    public HealthCheckResult checkRedisHealth() {
        return healthCheckFutures.computeIfAbsent("redis", key ->
            CompletableFuture.supplyAsync(() -> {
                try {
                    if (redisTemplate != null) {
                        String testKey = "health_check_" + System.currentTimeMillis();
                        String testValue = "ok";

                        redisTemplate.opsForValue().set(testKey, testValue, 10, TimeUnit.SECONDS);
                        String result = (String) redisTemplate.opsForValue().get(testKey);

                        if (testValue.equals(result)) {
                            metricsService.incrementCounter("health.check.redis", "result", "success");
                            return HealthCheckResult.healthy("Redis connection is healthy");
                        } else {
                            metricsService.incrementCounter("health.check.redis", "result", "error");
                            return HealthCheckResult.unhealthy("Redis test failed: value mismatch");
                        }
                    } else {
                        return HealthCheckResult.healthy("Redis not configured");
                    }
                } catch (Exception e) {
                    log.error("Redis health check failed", e);
                    metricsService.incrementCounter("health.check.redis", "result", "error");
                    return HealthCheckResult.unhealthy("Redis connection failed: " + e.getMessage())
                            .withDetail("error", e.getClass().getSimpleName());
                }
            })
        ).join();
    }

    /**
     * Check memory health
     */
    public HealthCheckResult checkMemoryHealth() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        long usedMemory = totalMemory - freeMemory;

        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;

        boolean healthy = memoryUsagePercent < 90;
        String status = healthy ? "healthy" : "warning";

        metricsService.recordGauge("jvm.memory.usage.percent", memoryUsagePercent, "type", "heap");
        metricsService.incrementCounter("health.check.memory", "result", status);

        if (healthy) {
            return HealthCheckResult.healthy("Memory usage is normal")
                    .withDetail("total_memory", totalMemory)
                    .withDetail("used_memory", usedMemory)
                    .withDetail("free_memory", freeMemory)
                    .withDetail("max_memory", maxMemory)
                    .withDetail("usage_percent", String.format("%.2f%%", memoryUsagePercent));
        } else {
            return HealthCheckResult.unhealthy("High memory usage: " + String.format("%.2f%%", memoryUsagePercent))
                    .withDetail("total_memory", totalMemory)
                    .withDetail("used_memory", usedMemory)
                    .withDetail("max_memory", maxMemory)
                    .withDetail("usage_percent", memoryUsagePercent);
        }
    }

    /**
     * Check disk space health
     */
    public HealthCheckResult checkDiskSpaceHealth() {
        try {
            java.io.File disk = new java.io.File("/");
            long totalSpace = disk.getTotalSpace();
            long freeSpace = disk.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;

            double diskUsagePercent = (double) usedSpace / totalSpace * 100;

            boolean healthy = diskUsagePercent < 85;
            String status = healthy ? "healthy" : "warning";

            metricsService.recordGauge("disk.usage.percent", diskUsagePercent);
            metricsService.incrementCounter("health.check.disk", "result", status);

            if (healthy) {
                return HealthCheckResult.healthy("Disk space is sufficient")
                        .withDetail("total_space", totalSpace)
                        .withDetail("used_space", usedSpace)
                        .withDetail("free_space", freeSpace)
                        .withDetail("usage_percent", String.format("%.2f%%", diskUsagePercent));
            } else {
                return HealthCheckResult.unhealthy("Low disk space: " + String.format("%.2f%%", diskUsagePercent))
                        .withDetail("total_space", totalSpace)
                        .withDetail("used_space", usedSpace)
                        .withDetail("free_space", freeSpace)
                        .withDetail("usage_percent", diskUsagePercent);
            }
        } catch (Exception e) {
            log.error("Disk space health check failed", e);
            metricsService.incrementCounter("health.check.disk", "result", "error");
            return HealthCheckResult.unhealthy("Failed to check disk space: " + e.getMessage())
                    .withDetail("error", e.getClass().getSimpleName());
        }
    }

    /**
     * Get overall application health
     */
    public Health getOverallHealth() {
        Health.Builder builder = Health.up();

        // Check database health
        HealthCheckResult dbHealth = checkDatabaseHealth();
        if (dbHealth.isHealthy()) {
            builder.withDetail("database", dbHealth.getDetails());
        } else {
            builder.status(Status.DOWN).withDetail("database", dbHealth.getDetails());
        }

        // Check Redis health
        HealthCheckResult redisHealth = checkRedisHealth();
        if (redisHealth.isHealthy()) {
            builder.withDetail("redis", redisHealth.getDetails());
        } else {
            builder.status(Status.DOWN).withDetail("redis", redisHealth.getDetails());
        }

        // Check memory health
        HealthCheckResult memoryHealth = checkMemoryHealth();
        builder.withDetail("memory", memoryHealth.getDetails());

        // Check disk space health
        HealthCheckResult diskHealth = checkDiskSpaceHealth();
        builder.withDetail("disk", diskHealth.getDetails());

        return builder.build();
    }

    /**
     * Clear health check cache
     */
    public void clearHealthCheckCache() {
        healthCheckCache.clear();
        healthCheckFutures.clear();
        log.info("Health check cache cleared");
    }

    /**
     * Get cached health check result
     */
    public HealthCheckResult getCachedHealthCheck(String checkName) {
        HealthCheckResult cached = healthCheckCache.get(checkName);
        if (cached != null && cached.getTimestamp().isAfter(LocalDateTime.now().minusMinutes(1))) {
            return cached;
        }
        return null;
    }

    /**
     * Cache health check result
     */
    public void cacheHealthCheckResult(String checkName, HealthCheckResult result) {
        healthCheckCache.put(checkName, result);
    }
}