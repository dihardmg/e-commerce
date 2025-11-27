package com.ecommerce.event.config;

import com.ecommerce.event.publisher.EventPublisher;
import com.ecommerce.event.handler.EventDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Event Configuration
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class EventConfiguration {

    private final ConnectionFactory connectionFactory;

    @Value("${app.events.rabbitmq.exchange:ecommerce.events}")
    private String eventsExchange;

    @Value("${app.events.rabbitmq.dlq:ecommerce.events.dlq}")
    private String deadLetterQueue;

    @Value("${app.events.rabbitmq.dlx:ecommerce.events.dlx}")
    private String deadLetterExchange;

    /**
     * Message converter for JSON serialization
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate configuration
     */
    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());

        // Enable publisher confirms
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("Message confirmed: {}", correlationData);
            } else {
                log.error("Message not confirmed: {} - {}", correlationData, cause);
            }
        });

        // Enable publisher returns
        template.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            log.error("Message returned: {} {} {} {} {}",
                replyCode, replyText, exchange, routingKey, message);
        });

        return template;
    }

    /**
     * Events exchange
     */
    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(eventsExchange, true, false);
    }

    /**
     * Dead letter exchange
     */
    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(deadLetterExchange, true, false);
    }

    /**
     * Dead letter queue
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(deadLetterQueue)
                .withArgument("x-dead-letter-exchange", eventsExchange)
                .build();
    }

    /**
     * Bind dead letter queue to dead letter exchange
     */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("#");
    }

    /**
     * Rabbit listener container factory
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(5);
        factory.setPrefetchCount(10);

        // Enable retry
        factory.setAdviceChain(org.springframework.amqp.rabbit.config.RetryInterceptorBuilder
                .stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2, 5000)
                .build());

        return factory;
    }

    /**
     * Event publisher bean
     */
    @Bean
    public EventPublisher eventPublisher(RabbitTemplate rabbitTemplate,
                                     org.springframework.cloud.stream.function.StreamBridge streamBridge,
                                     RedisTemplate<String, Object> redisTemplate,
                                     org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate) {
        return new EventPublisher(rabbitTemplate, streamBridge, redisTemplate, kafkaTemplate);
    }

    /**
     * Event dispatcher bean
     */
    @Bean
    public EventDispatcher eventDispatcher(org.springframework.context.ApplicationContext applicationContext) {
        return new EventDispatcher(applicationContext);
    }
}