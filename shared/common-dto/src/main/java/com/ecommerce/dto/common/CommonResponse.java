package com.ecommerce.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Standard response wrapper for all API endpoints
 *
 * @param <T> Type of the response data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response wrapper")
public class CommonResponse<T> {

    @Schema(description = "Success status of the operation", example = "true")
    private boolean success;

    @Schema(description = "Response data")
    private T data;

    @Schema(description = "Error information (only present when success is false)")
    private ErrorResponse error;

    @Schema(description = "Response metadata")
    private ResponseMetadata metadata;

    /**
     * Create success response with data
     */
    public static <T> CommonResponse<T> success(T data) {
        return CommonResponse.<T>builder()
                .success(true)
                .data(data)
                .metadata(ResponseMetadata.builder()
                        .timestamp(Instant.now())
                        .build())
                .build();
    }

    /**
     * Create success response with data and metadata
     */
    public static <T> CommonResponse<T> success(T data, ResponseMetadata metadata) {
        return CommonResponse.<T>builder()
                .success(true)
                .data(data)
                .metadata(metadata)
                .build();
    }

    /**
     * Create error response
     */
    public static <T> CommonResponse<T> error(String code, String message) {
        return CommonResponse.<T>builder()
                .success(false)
                .error(ErrorResponse.builder()
                        .code(code)
                        .message(message)
                        .timestamp(Instant.now())
                        .build())
                .metadata(ResponseMetadata.builder()
                        .timestamp(Instant.now())
                        .build())
                .build();
    }

    /**
     * Create error response with detailed error
     */
    public static <T> CommonResponse<T> error(ErrorResponse error) {
        return CommonResponse.<T>builder()
                .success(false)
                .error(error)
                .metadata(ResponseMetadata.builder()
                        .timestamp(Instant.now())
                        .build())
                .build();
    }
}