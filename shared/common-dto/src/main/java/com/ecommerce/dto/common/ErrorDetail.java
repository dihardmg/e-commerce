package com.ecommerce.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Detailed error information for validation errors
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Detailed error information")
public class ErrorDetail {

    @Schema(description = "Field name that caused the error", example = "productId")
    private String field;

    @Schema(description = "Rejected value", example = "123")
    private Object rejectedValue;

    @Schema(description = "Error message for the field", example = "Product does not exist")
    private String message;

    @Schema(description = "Error code for the field", example = "INVALID_PRODUCT_ID")
    private String code;
}