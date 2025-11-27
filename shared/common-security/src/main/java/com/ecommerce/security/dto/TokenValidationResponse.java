package com.ecommerce.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Token Validation Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponse {

    private boolean valid;
    private String username;
    private List<String> authorities;
    private String errorMessage;
    private boolean expired;

    public static TokenValidationResponse valid(String username, List<String> authorities) {
        return TokenValidationResponse.builder()
                .valid(true)
                .username(username)
                .authorities(authorities)
                .expired(false)
                .build();
    }

    public static TokenValidationResponse invalid(String errorMessage) {
        return TokenValidationResponse.builder()
                .valid(false)
                .errorMessage(errorMessage)
                .expired(false)
                .build();
    }

    public static TokenValidationResponse expired(String errorMessage) {
        return TokenValidationResponse.builder()
                .valid(false)
                .errorMessage(errorMessage)
                .expired(true)
                .build();
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isExpired() {
        return expired;
    }
}