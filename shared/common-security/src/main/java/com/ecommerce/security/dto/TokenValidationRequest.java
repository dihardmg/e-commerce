package com.ecommerce.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * Token Validation Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationRequest {

    @NotBlank(message = "Token cannot be blank")
    private String token;

    private String expectedUsername;

    private String requiredAuthority;
}