package com.ecommerce.monitoring.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Monitoring Configuration
 */
@Configuration
@EnableAspectJAutoProxy
@RequiredArgsConstructor
public class MonitoringConfiguration {

    /**
     * Meter registry customizer
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags(
            "application", "ecommerce-microservices",
            "region", System.getProperty("region", "default"),
            "environment", System.getProperty("spring.profiles.active", "default")
        );
    }

    /**
     * Enable @Timed annotation support
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * Health check indicator
     */
    @Bean
    public org.springframework.boot.actuate.health.HealthIndicator healthIndicator(
            com.ecommerce.monitoring.service.HealthCheckService healthCheckService) {
        return () -> healthCheckService.getOverallHealth();
    }

    /**
     * Metrics service bean
     */
    @Bean
    public com.ecommerce.monitoring.service.MetricsService metricsService(MeterRegistry meterRegistry) {
        return new com.ecommerce.monitoring.service.MetricsService(meterRegistry);
    }

    /**
     * Health check service bean
     */
    @Bean
    public com.ecommerce.monitoring.service.HealthCheckService healthCheckService(
            com.ecommerce.monitoring.service.MetricsService metricsService) {
        return new com.ecommerce.monitoring.service.HealthCheckService(metricsService);
    }
}