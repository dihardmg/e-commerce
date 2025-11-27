package com.ecommerce.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Error response wrapper for API errors
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Error response information")
public class ErrorResponse {

    @Schema(description = "Error code", example = "RESOURCE_NOT_FOUND")
    private String code;

    @Schema(description = "Error message", example = "Product with ID 123 not found")
    private String message;

    @Schema(description = "Detailed error information")
    private List<ErrorDetail> details;

    @Schema(description = "Request path where error occurred", example = "/api/v1/products/123")
    private String path;

    @Schema(description = "Timestamp when error occurred")
    private Instant timestamp;

    @Schema(description = "Request ID for tracing", example = "req_123456789")
    private String requestId;
}