package com.ecommerce.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Security Configuration Properties
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    /**
     * JWT Configuration
     */
    private final Jwt jwt = new Jwt();

    /**
     * CORS Configuration
     */
    private final Cors cors = new Cors();

    /**
     * Rate Limiting Configuration
     */
    private final RateLimit rateLimit = new RateLimit();

    /**
     * JWT Configuration Properties
     */
    @Data
    public static class Jwt {
        /**
         * Access token expiration time in seconds (default: 15 minutes)
         */
        private long accessTokenExpiration = 900;

        /**
         * Refresh token expiration time in seconds (default: 30 days)
         */
        private long refreshTokenExpiration = 2592000;

        /**
         * Token issuer
         */
        private String issuer = "ecommerce-platform";

        /**
         * Clock skew tolerance in seconds (default: 60 seconds)
         */
        private long clockSkew = 60;

        /**
         * Enable JWT token blacklisting
         */
        private boolean blacklistingEnabled = true;

        /**
         * Blacklist cleanup interval in hours (default: 24 hours)
         */
        private long blacklistCleanupInterval = 24;
    }

    /**
     * CORS Configuration Properties
     */
    @Data
    public static class Cors {
        /**
         * Enable CORS support
         */
        private boolean enabled = true;

        /**
         * Allowed origins
         */
        private String[] allowedOrigins = {"http://localhost:3000", "http://localhost:8080"};

        /**
         * Allowed HTTP methods
         */
        private String[] allowedMethods = {"GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"};

        /**
         * Allowed headers
         */
        private String[] allowedHeaders = {"*"};

        /**
         * Exposed headers
         */
        private String[] exposedHeaders = {"Authorization", "Content-Type"};

        /**
         * Allow credentials
         */
        private boolean allowCredentials = true;

        /**
         * Max age for pre-flight requests
         */
        private long maxAge = 3600;
    }

    /**
     * Rate Limiting Configuration Properties
     */
    @Data
    public static class RateLimit {
        /**
         * Enable rate limiting
         */
        private boolean enabled = true;

        /**
         * Default requests per minute
         */
        private int requestsPerMinute = 100;

        /**
         * Strict endpoints (lower rate limits)
         */
        private String[] strictEndpoints = {"/auth/**", "/api/auth/**"};

        /**
         * Requests per minute for strict endpoints
         */
        private int strictRequestsPerMinute = 10;

        /**
         * Admin endpoints
         */
        private String[] adminEndpoints = {"/admin/**", "/api/admin/**"};

        /**
         * Requests per minute for admin endpoints
         */
        private int adminRequestsPerMinute = 200;
    }
}