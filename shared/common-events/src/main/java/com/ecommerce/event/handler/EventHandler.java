package com.ecommerce.event.handler;

import com.ecommerce.event.BaseEvent;

/**
 * Event Handler Interface
 *
 * All event handlers should implement this interface
 */
public interface EventHandler<T extends BaseEvent> {

    /**
     * Handle the event
     *
     * @param event the event to handle
     */
    void handle(T event);

    /**
     * Get the event type this handler supports
     *
     * @return the event class
     */
    Class<T> getEventType();

    /**
     * Check if this handler should process the event
     *
     * @param event the event to check
     * @return true if the handler should process the event
     */
    default boolean canHandle(BaseEvent event) {
        return getEventType().isInstance(event);
    }

    /**
     * Get handler priority (lower number = higher priority)
     *
     * @return priority value
     */
    default int getPriority() {
        return 100;
    }

    /**
     * Get handler name for logging and debugging
     *
     * @return handler name
     */
    default String getHandlerName() {
        return this.getClass().getSimpleName();
    }
}