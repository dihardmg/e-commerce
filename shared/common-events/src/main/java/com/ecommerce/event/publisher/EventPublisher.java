package com.ecommerce.event.publisher;

import com.ecommerce.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Event Publisher
 *
 * Handles publishing events to different message brokers
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final StreamBridge streamBridge;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.events.rabbitmq.exchange:ecommerce.events}")
    private String rabbitMqExchange;

    @Value("${app.events.redis.enabled:true}")
    private boolean redisEnabled;

    @Value("${app.events.kafka.enabled:false}")
    private boolean kafkaEnabled;

    @Value("${app.events.kafka.topic:ecommerce-events}")
    private String kafkaTopic;

    @Value("${app.events.redis.ttl:86400}")
    private long redisTtlSeconds;

    /**
     * Publish event to all configured channels
     */
    public void publishEvent(BaseEvent event) {
        publishEvent(event, event.getEventType());
    }

    /**
     * Publish event to all configured channels with custom routing key
     */
    public void publishEvent(BaseEvent event, String routingKey) {
        try {
            // Publish to RabbitMQ
            publishToRabbitMQ(event, routingKey);

            // Publish to Spring Cloud Stream
            publishToStreamBridge(event, routingKey);

            // Publish to Redis (for local caching/event replay)
            if (redisEnabled) {
                publishToRedis(event);
            }

            // Publish to Kafka (if enabled)
            if (kafkaEnabled) {
                publishToKafka(event, routingKey);
            }

            log.debug("Event published successfully: {} - {}", event.getEventType(), event.getEventId());

        } catch (Exception e) {
            log.error("Failed to publish event: {} - {}", event.getEventType(), e.getMessage(), e);
            throw new EventPublishException("Failed to publish event: " + event.getEventType(), e);
        }
    }

    /**
     * Publish to RabbitMQ
     */
    private void publishToRabbitMQ(BaseEvent event, String routingKey) {
        try {
            rabbitTemplate.convertAndSend(rabbitMqExchange, routingKey, event);
            log.debug("Event published to RabbitMQ: {} -> {}", routingKey, event.getEventId());
        } catch (Exception e) {
            log.error("Failed to publish event to RabbitMQ: {}", routingKey, e);
        }
    }

    /**
     * Publish to Spring Cloud Stream
     */
    private void publishToStreamBridge(BaseEvent event, String routingKey) {
        try {
            streamBridge.send("eventPublisher-out-0", event);
            log.debug("Event published to Stream Bridge: {} -> {}", routingKey, event.getEventId());
        } catch (Exception e) {
            log.error("Failed to publish event to Stream Bridge: {}", routingKey, e);
        }
    }

    /**
     * Publish to Redis
     */
    private void publishToRedis(BaseEvent event) {
        try {
            String key = String.format("event:%s:%s", event.getEventType(), event.getEventId());
            redisTemplate.opsForValue().set(key, event, Duration.ofSeconds(redisTtlSeconds));

            // Also add to recent events list for each event type
            String recentEventsKey = String.format("events:recent:%s", event.getEventType());
            redisTemplate.opsForList().leftPush(recentEventsKey, event);
            redisTemplate.expire(recentEventsKey, Duration.ofHours(24));

            log.debug("Event published to Redis: {} -> {}", key, event.getEventId());
        } catch (Exception e) {
            log.error("Failed to publish event to Redis: {}", event.getEventType(), e);
        }
    }

    /**
     * Publish to Kafka
     */
    private void publishToKafka(BaseEvent event, String routingKey) {
        try {
            kafkaTemplate.send(kafkaTopic, routingKey, event);
            log.debug("Event published to Kafka: {} -> {}", routingKey, event.getEventId());
        } catch (Exception e) {
            log.error("Failed to publish event to Kafka: {}", routingKey, e);
        }
    }

    /**
     * Publish event with delay
     */
    public void publishEventWithDelay(BaseEvent event, Duration delay) {
        // Store delayed event in Redis
        String delayKey = String.format("delayed-event:%s:%s",
            System.currentTimeMillis() + delay.toMillis(), event.getEventId());
        redisTemplate.opsForValue().set(delayKey, event, delay);

        log.info("Event scheduled for delayed publication: {} in {} seconds",
            event.getEventType(), delay.get(ChronoUnit.SECONDS));
    }

    /**
     * Publish event with retry
     */
    public void publishEventWithRetry(BaseEvent event, int maxRetries) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetries) {
            try {
                publishEvent(event);
                return;
            } catch (Exception e) {
                lastException = e;
                attempt++;
                log.warn("Event publishing attempt {} failed: {} - {}",
                    attempt, event.getEventType(), e.getMessage());

                if (attempt < maxRetries) {
                    try {
                        // Exponential backoff
                        Thread.sleep(1000L * (1L << (attempt - 1)));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        log.error("Failed to publish event after {} attempts: {}", maxRetries, event.getEventType());
        throw new EventPublishException("Event publishing failed after " + maxRetries + " attempts", lastException);
    }

    /**
     * Check if event was already processed (idempotency)
     */
    public boolean isEventProcessed(String eventId) {
        try {
            String key = String.format("processed-event:%s", eventId);
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.warn("Failed to check event processing status: {}", eventId, e);
            return false;
        }
    }

    /**
     * Mark event as processed
     */
    public void markEventProcessed(String eventId) {
        try {
            String key = String.format("processed-event:%s", eventId);
            redisTemplate.opsForValue().set(key, true, Duration.ofDays(7));
        } catch (Exception e) {
            log.warn("Failed to mark event as processed: {}", eventId, e);
        }
    }
}