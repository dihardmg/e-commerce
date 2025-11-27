package com.ecommerce.event.handler;

import com.ecommerce.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Event Dispatcher
 *
 * Dispatches events to appropriate handlers
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventDispatcher {

    private final ApplicationContext applicationContext;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Dispatch event synchronously to all handlers
     */
    public void dispatch(BaseEvent event) {
        List<EventHandler<?>> handlers = findHandlers(event);

        if (handlers.isEmpty()) {
            log.warn("No handlers found for event type: {}", event.getEventType());
            return;
        }

        for (EventHandler<?> handler : handlers) {
            try {
                dispatchToHandler(event, handler);
            } catch (Exception e) {
                log.error("Failed to dispatch event {} to handler {}",
                    event.getEventType(), handler.getHandlerName(), e);
            }
        }
    }

    /**
     * Dispatch event asynchronously to all handlers
     */
    public CompletableFuture<Void> dispatchAsync(BaseEvent event) {
        return CompletableFuture.runAsync(() -> dispatch(event), executorService);
    }

    /**
     * Dispatch event to specific handler type
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseEvent> void dispatchToSpecificHandler(T event, Class<T> eventType) {
        List<EventHandler<?>> handlers = applicationContext.getBeansOfType(EventHandler.class).values()
                .stream()
                .filter(handler -> eventType.equals(handler.getEventType()))
                .sorted(Comparator.comparingInt(EventHandler::getPriority))
                .toList();

        for (EventHandler<?> handler : handlers) {
            try {
                dispatchToHandler(event, handler);
            } catch (Exception e) {
                log.error("Failed to dispatch event {} to specific handler {}",
                    event.getEventType(), handler.getHandlerName(), e);
            }
        }
    }

    /**
     * Dispatch event and wait for all handlers to complete
     */
    public void dispatchAndWait(BaseEvent event, long timeout, TimeUnit timeUnit) {
        List<CompletableFuture<Void>> futures = List.of(dispatchAsync(event));

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(timeout, timeUnit);
        } catch (Exception e) {
            log.error("Failed to wait for event handlers to complete: {}", event.getEventType(), e);
        }
    }

    /**
     * Find handlers for the event
     */
    @SuppressWarnings("unchecked")
    private <T extends BaseEvent> List<EventHandler<T>> findHandlers(BaseEvent event) {
        return applicationContext.getBeansOfType(EventHandler.class).values()
                .stream()
                .filter(handler -> handler.canHandle(event))
                .map(handler -> (EventHandler<T>) handler)
                .sorted(Comparator.comparingInt(EventHandler::getPriority))
                .toList();
    }

    /**
     * Dispatch event to a specific handler
     */
    @SuppressWarnings("unchecked")
    private <T extends BaseEvent> void dispatchToHandler(BaseEvent event, EventHandler<T> handler) {
        T typedEvent = (T) event;

        log.debug("Dispatching event {} to handler {}", event.getEventType(), handler.getHandlerName());

        long startTime = System.currentTimeMillis();
        try {
            handler.handle(typedEvent);
            long duration = System.currentTimeMillis() - startTime;
            log.debug("Handler {} processed event {} in {}ms",
                handler.getHandlerName(), event.getEventType(), duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Handler {} failed to process event {} in {}ms",
                handler.getHandlerName(), event.getEventType(), duration, e);
            throw e;
        }
    }

    /**
     * Shutdown the dispatcher
     */
    public void shutdown() {
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}