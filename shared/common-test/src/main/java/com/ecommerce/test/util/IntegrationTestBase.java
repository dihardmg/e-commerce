package com.ecommerce.test.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Integration Test Base
 *
 * Base class for integration tests with testcontainers
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Testcontainers
public abstract class IntegrationTestBase {

    @Container
    protected static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:17-alpine"))
                    .withDatabaseName("test_db")
                    .withUsername("test_user")
                    .withPassword("test_password")
                    .withReuse(true);

    @Container
    protected static final RabbitMQContainer rabbitmqContainer =
            new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.12-management-alpine"))
                    .withReuse(true);

    @Container
    protected static final GenericContainer<?> redisContainer =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379)
                    .withReuse(true);

    @BeforeEach
    void setUp() {
        // Override properties for testcontainers
        System.setProperty("spring.datasource.url", postgresContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgresContainer.getUsername());
        System.setProperty("spring.datasource.password", postgresContainer.getPassword());
        System.setProperty("spring.datasource.driver-class-name", postgresContainer.getDriverClassName());

        System.setProperty("spring.rabbitmq.host", rabbitmqContainer.getHost());
        System.setProperty("spring.rabbitmq.port", rabbitmqContainer.getAmqpPort().toString());
        System.setProperty("spring.rabbitmq.username", rabbitmqContainer.getAdminUsername());
        System.setProperty("spring.rabbitmq.password", rabbitmqContainer.getAdminPassword());

        System.setProperty("spring.data.redis.host", redisContainer.getHost());
        System.setProperty("spring.data.redis.port", redisContainer.getMappedPort(6379).toString());
    }

    /**
     * Dynamic property source for testcontainers
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgresContainer::getDriverClassName);

        // RabbitMQ
        registry.add("spring.rabbitmq.host", rabbitmqContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitmqContainer::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitmqContainer::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitmqContainer::getAdminPassword);

        // Redis
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379).toString());

        // Test properties
        registry.add("app.security.jwt.issuer", () -> "test-issuer");
        registry.add("app.security.jwt.access-token-expiration", () -> "60");
        registry.add("app.security.jwt.refresh-token-expiration", () -> "3600");

        registry.add("app.events.rabbitmq.exchange", () -> "test.events");
        registry.add("app.events.redis.enabled", () -> "false");
        registry.add("app.events.kafka.enabled", () -> "false");
    }

    /**
     * Get database URL
     */
    protected String getDatabaseUrl() {
        return postgresContainer.getJdbcUrl();
    }

    /**
     * Get RabbitMQ host
     */
    protected String getRabbitMQHost() {
        return rabbitmqContainer.getHost();
    }

    /**
     * Get RabbitMQ port
     */
    protected Integer getRabbitMQPort() {
        return rabbitmqContainer.getAmqpPort();
    }

    /**
     * Get Redis host
     */
    protected String getRedisHost() {
        return redisContainer.getHost();
    }

    /**
     * Get Redis port
     */
    protected Integer getRedisPort() {
        return redisContainer.getMappedPort(6379);
    }

    /**
     * Wait for containers to be ready
     */
    protected void waitForContainers() {
        postgresContainer.isRunning();
        rabbitmqContainer.isRunning();
        redisContainer.isRunning();
    }
}