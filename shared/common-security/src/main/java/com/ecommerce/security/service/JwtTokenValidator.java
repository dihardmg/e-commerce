package com.ecommerce.security.service;

import com.ecommerce.security.dto.TokenValidationRequest;
import com.ecommerce.security.dto.TokenValidationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * JWT Token Validator Service
 *
 * Handles validation of JWT tokens for authentication and authorization
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenValidator {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Validate JWT token
     */
    public TokenValidationResponse validateToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return TokenValidationResponse.invalid("Token is null or empty");
            }

            // Remove Bearer prefix if present
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            if (jwtTokenProvider.isTokenExpired(token)) {
                return TokenValidationResponse.expired("Token has expired");
            }

            String username = jwtTokenProvider.getUsernameFromToken(token);
            if (username == null) {
                return TokenValidationResponse.invalid("Invalid token - username not found");
            }

            return TokenValidationResponse.valid(username,
                jwtTokenProvider.getAuthoritiesFromToken(token));

        } catch (Exception e) {
            log.error("Token validation failed", e);
            return TokenValidationResponse.invalid("Token validation failed: " + e.getMessage());
        }
    }

    /**
     * Validate token for specific user
     */
    public boolean validateTokenForUser(String token, String username) {
        TokenValidationResponse validation = validateToken(token);
        return validation.isValid() && username.equals(validation.getUsername());
    }

    /**
     * Check if token has specific authority
     */
    public boolean tokenHasAuthority(String token, String authority) {
        TokenValidationResponse validation = validateToken(token);
        return validation.isValid() && validation.getAuthorities().contains(authority);
    }

    /**
     * Extract username from token (with validation)
     */
    public String extractUsername(String token) {
        TokenValidationResponse validation = validateToken(token);
        return validation.isValid() ? validation.getUsername() : null;
    }
}