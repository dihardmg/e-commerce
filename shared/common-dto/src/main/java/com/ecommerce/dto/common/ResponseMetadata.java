package com.ecommerce.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response metadata containing pagination and request information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response metadata")
public class ResponseMetadata {

    @Schema(description = "Response timestamp")
    private Instant timestamp;

    @Schema(description = "Request ID for tracing", example = "req_123456789")
    private String requestId;

    @Schema(description = "API version", example = "v1")
    private String version;

    @Schema(description = "Pagination information (for list responses)")
    private PaginationInfo pagination;

    /**
     * Create metadata with default values
     */
    public static ResponseMetadata create() {
        return ResponseMetadata.builder()
                .timestamp(Instant.now())
                .version("v1")
                .build();
    }

    /**
     * Create metadata with pagination
     */
    public static ResponseMetadata withPagination(PaginationInfo pagination) {
        return ResponseMetadata.builder()
                .timestamp(Instant.now())
                .version("v1")
                .pagination(pagination)
                .build();
    }

    /**
     * Create metadata with request ID
     */
    public static ResponseMetadata withRequestId(String requestId) {
        return ResponseMetadata.builder()
                .timestamp(Instant.now())
                .version("v1")
                .requestId(requestId)
                .build();
    }
}