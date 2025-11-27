package com.ecommerce.event.publisher;

import com.ecommerce.exception.ECommerceException;

/**
 * Event Publish Exception
 *
 * Thrown when event publishing fails
 */
public class EventPublishException extends ECommerceException {

    public EventPublishException(String message) {
        super("EVENT_PUBLISH_ERROR", message);
    }

    public EventPublishException(String message, Throwable cause) {
        super("EVENT_PUBLISH_ERROR", message, cause);
    }

    public EventPublishException(String errorCode, String message) {
        super(errorCode, message);
    }

    public EventPublishException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public EventPublishException(String errorCode, String serviceCode, String message) {
        super(errorCode, serviceCode, message);
    }

    public EventPublishException(String errorCode, String serviceCode, String message, Throwable cause) {
        super(errorCode, serviceCode, message, cause);
    }
}