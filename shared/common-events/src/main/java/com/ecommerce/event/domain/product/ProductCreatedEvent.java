package com.ecommerce.event.domain.product;

import com.ecommerce.event.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Product Created Event
 *
 * Fired when a new product is created
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class ProductCreatedEvent extends BaseEvent {

    /**
     * Product ID
     */
    private String productId;

    /**
     * Product SKU
     */
    private String sku;

    /**
     * Product name
     */
    private String name;

    /**
     * Product description
     */
    private String description;

    /**
     * Category ID
     */
    private String categoryId;

    /**
     * Brand
     */
    private String brand;

    /**
     * Price
     */
    private BigDecimal price;

    /**
     * Currency code
     */
    private String currency;

    /**
     * Available stock
     */
    private Integer availableStock;

    /**
     * Minimum stock level
     */
    private Integer minimumStock;

    /**
     * Maximum stock level
     */
    private Integer maximumStock;

    /**
     * Weight
     */
    private BigDecimal weight;

    /**
     * Weight unit
     */
    private String weightUnit;

    /**
     * Dimensions
     */
    private String dimensions;

    /**
     * Tags
     */
    private java.util.List<String> tags;

    /**
     * Attributes
     */
    private java.util.Map<String, Object> attributes;

    /**
     * Whether product is active
     */
    private Boolean active;

    /**
     * Created by
     */
    private String createdBy;

    public ProductCreatedEvent(String sourceService) {
        super(sourceService);
    }

    @Override
    public String getEventType() {
        return "PRODUCT_CREATED";
    }
}