package com.ecommerce.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pagination information for list responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Pagination information")
public class PaginationInfo {

    @Schema(description = "Current page number (0-based)", example = "0")
    private int page;

    @Schema(description = "Number of items per page", example = "20")
    private int size;

    @Schema(description = "Total number of elements", example = "100")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "5")
    private int totalPages;

    @Schema(description = "Whether this is the first page", example = "true")
    private boolean first;

    @Schema(description = "Whether this is the last page", example = "false")
    private boolean last;

    @Schema(description = "Number of elements on current page", example = "20")
    private int numberOfElements;

    @Schema(description = "Whether there are next pages", example = "true")
    private boolean hasNext;

    @Schema(description = "Whether there are previous pages", example = "false")
    private boolean hasPrevious;

    /**
     * Create pagination info from page details
     */
    public static PaginationInfo of(int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int numberOfElements = Math.min(size, (int) Math.max(0, totalElements - (long) page * size));

        return PaginationInfo.builder()
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .numberOfElements(numberOfElements)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();
    }
}