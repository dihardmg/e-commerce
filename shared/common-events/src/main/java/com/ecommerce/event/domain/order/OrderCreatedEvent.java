package com.ecommerce.event.domain.order;

import com.ecommerce.event.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order Created Event
 *
 * Fired when a new order is created
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class OrderCreatedEvent extends BaseEvent {

    /**
     * Order ID
     */
    private String orderId;

    /**
     * Order number
     */
    private String orderNumber;

    /**
     * Customer ID
     */
    private String customerId;

    /**
     * Customer email
     */
    private String customerEmail;

    /**
     * Order status
     */
    private String orderStatus;

    /**
     * Order date
     */
    private LocalDateTime orderDate;

    /**
     * Expected delivery date
     */
    private LocalDateTime expectedDeliveryDate;

    /**
     * Subtotal amount
     */
    private BigDecimal subtotal;

    /**
     * Tax amount
     */
    private BigDecimal taxAmount;

    /**
     * Shipping amount
     */
    private BigDecimal shippingAmount;

    /**
     * Discount amount
     */
    private BigDecimal discountAmount;

    /**
     * Total amount
     */
    private BigDecimal totalAmount;

    /**
     * Currency code
     */
    private String currency;

    /**
     * Payment method
     */
    private String paymentMethod;

    /**
     * Payment status
     */
    private String paymentStatus;

    /**
     * Shipping address
     */
    private AddressInfo shippingAddress;

    /**
     * Billing address
     */
    private AddressInfo billingAddress;

    /**
     * Order items
     */
    private java.util.List<OrderItemInfo> orderItems;

    /**
     * Order notes
     */
    private String orderNotes;

    /**
     * Shipping method
     */
    private String shippingMethod;

    /**
     * Tracking number
     */
    private String trackingNumber;

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressInfo {
        private String street;
        private String city;
        private String state;
        private String zipCode;
        private String country;
        private String phone;
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemInfo {
        private String productId;
        private String sku;
        private String productName;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal totalPrice;
        private java.util.Map<String, Object> productAttributes;
    }

    public OrderCreatedEvent(String sourceService) {
        super(sourceService);
    }

    @Override
    public String getEventType() {
        return "ORDER_CREATED";
    }
}