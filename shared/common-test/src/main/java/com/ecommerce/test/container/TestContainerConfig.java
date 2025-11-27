package com.ecommerce.test.container;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.time.Duration;

/**
 * Test Container Configuration
 *
 * Provides test container configurations for integration tests
 */
@TestConfiguration
public class TestContainerConfig {

    private static final String POSTGRES_IMAGE = "postgres:17-alpine";
    private static final String RABBITMQ_IMAGE = "rabbitmq:3.12-management-alpine";
    private static final String REDIS_IMAGE = "redis:7-alpine";
    private static final String ELASTICSEARCH_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch:8.10.0";
    private static final String KAFKA_IMAGE = "confluentinc/cp-kafka:7.4.0";

    /**
     * PostgreSQL test container
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @Primary
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(POSTGRES_IMAGE)
                .withDatabaseName("test_db")
                .withUsername("test_user")
                .withPassword("test_password")
                .withReuse(true)
                .withStartupTimeout(Duration.ofMinutes(2));
    }

    /**
     * RabbitMQ test container
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @Primary
    public RabbitMQContainer rabbitmqContainer() {
        return new RabbitMQContainer(RABBITMQ_IMAGE)
                .withReuse(true)
                .withStartupTimeout(Duration.ofMinutes(2));
    }

    /**
     * Redis test container
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @Primary
    public GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE))
                .withExposedPorts(6379)
                .withReuse(true)
                .withStartupTimeout(Duration.ofMinutes(1));
    }

    /**
     * Elasticsearch test container
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @Primary
    public GenericContainer<?> elasticsearchContainer() {
        return new GenericContainer<>(DockerImageName.parse(ELASTICSEARCH_IMAGE))
                .withEnv("discovery.type", "single-node")
                .withEnv("xpack.security.enabled", "false")
                .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
                .withExposedPorts(9200)
                .withReuse(true)
                .withStartupTimeout(Duration.ofMinutes(3));
    }

    /**
     * Kafka test container
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @Primary
    public GenericContainer<?> kafkaContainer() {
        return new GenericContainer<>(DockerImageName.parse(KAFKA_IMAGE))
                .withEnv("KAFKA_BROKER_ID", "1")
                .withEnv("KAFKA_ZOOKEEPER_CONNECT", "zookeeper:2181")
                .withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://localhost:9092")
                .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
                .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
                .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
                .withEnv("KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS", "0")
                .withEnv("KAFKA_JMX_PORT", "9101")
                .withEnv("KAFKA_JMX_HOSTNAME", "localhost")
                .withExposedPorts(9092)
                .withReuse(true)
                .withStartupTimeout(Duration.ofMinutes(3));
    }

    /**
     * Test data source for PostgreSQL
     */
    @Bean
    @Primary
    public DataSource testDataSource(PostgreSQLContainer<?> postgresContainer) {
        org.springframework.boot.jdbc.DataSourceBuilder builder =
                org.springframework.boot.jdbc.DataSourceBuilder.create();

        builder.driverClassName(postgresContainer.getDriverClassName())
                .url(postgresContainer.getJdbcUrl())
                .username(postgresContainer.getUsername())
                .password(postgresContainer.getPassword());

        return builder.build();
    }
}