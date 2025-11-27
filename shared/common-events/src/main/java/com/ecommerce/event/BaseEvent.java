package com.ecommerce.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base Event Class
 *
 * All domain events should extend this class
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "eventType"
)
@JsonSubTypes({
    // User Events
    @JsonSubTypes.Type(value = UserRegisteredEvent.class, name = "USER_REGISTERED"),
    @JsonSubTypes.Type(value = UserUpdatedEvent.class, name = "USER_UPDATED"),
    @JsonSubTypes.Type(value = UserDeletedEvent.class, name = "USER_DELETED"),
    @JsonSubTypes.Type(value = UserActivatedEvent.class, name = "USER_ACTIVATED"),
    @JsonSubTypes.Type(value = UserDeactivatedEvent.class, name = "USER_DEACTIVATED"),

    // Product Events
    @JsonSubTypes.Type(value = ProductCreatedEvent.class, name = "PRODUCT_CREATED"),
    @JsonSubTypes.Type(value = ProductUpdatedEvent.class, name = "PRODUCT_UPDATED"),
    @JsonSubTypes.Type(value = ProductDeletedEvent.class, name = "PRODUCT_DELETED"),
    @JsonSubTypes.Type(value = ProductStockUpdatedEvent.class, name = "PRODUCT_STOCK_UPDATED"),

    // Order Events
    @JsonSubTypes.Type(value = OrderCreatedEvent.class, name = "ORDER_CREATED"),
    @JsonSubTypes.Type(value = OrderUpdatedEvent.class, name = "ORDER_UPDATED"),
    @JsonSubTypes.Type(value = OrderCancelledEvent.class, name = "ORDER_CANCELLED"),
    @JsonSubTypes.Type(value = OrderShippedEvent.class, name = "ORDER_SHIPPED"),
    @JsonSubTypes.Type(value = OrderDeliveredEvent.class, name = "ORDER_DELIVERED"),

    // Inventory Events
    @JsonSubTypes.Type(value = StockReservedEvent.class, name = "STOCK_RESERVED"),
    @JsonSubTypes.Type(value = StockReleasedEvent.class, name = "STOCK_RELEASED"),
    @JsonSubTypes.Type(value = StockUpdatedEvent.class, name = "STOCK_UPDATED"),

    // Notification Events
    @JsonSubTypes.Type(value = NotificationCreatedEvent.class, name = "NOTIFICATION_CREATED"),
    @JsonSubTypes.Type(value = EmailSentEvent.class, name = "EMAIL_SENT"),
    @JsonSubTypes.Type(value = SmsSentEvent.class, name = "SMS_SENT"),
    @JsonSubTypes.Type(value = PushNotificationSentEvent.class, name = "PUSH_NOTIFICATION_SENT")
})
public abstract class BaseEvent {

    /**
     * Unique event identifier
     */
    private String eventId;

    /**
     * Timestamp when event was created
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private LocalDateTime timestamp;

    /**
     * Service that generated the event
     */
    private String sourceService;

    /**
     * Event version for schema evolution
     */
    private String version;

    /**
     * Correlation ID for tracing related events
     */
    private String correlationId;

    /**
     * Causation ID linking to the command that caused this event
     */
    private String causationId;

    /**
     * User ID that triggered the event
     */
    private String userId;

    /**
     * Tenant ID for multi-tenancy support
     */
    private String tenantId;

    public BaseEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.version = "1.0";
    }

    public BaseEvent(String sourceService) {
        this();
        this.sourceService = sourceService;
    }

    public BaseEvent(String sourceService, String correlationId) {
        this(sourceService);
        this.correlationId = correlationId;
    }

    public BaseEvent(String sourceService, String correlationId, String userId) {
        this(sourceService, correlationId);
        this.userId = userId;
    }

    /**
     * Get event type (implemented by subclasses)
     */
    public abstract String getEventType();
}