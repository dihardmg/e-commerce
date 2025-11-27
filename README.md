# E-Commerce Microservice Case Study

## Overview

This case study demonstrates the implementation of a microservices architecture for an e-commerce platform using modern Java and Spring Boot technologies. The system handles user management, product catalog, order processing, and inventory management.

## Technology Stack

### Backend
- **Java**: 25
- **Framework**: Spring Boot 4.0.0
- **Database**: PostgreSQL 17
- **Build Tool**: Maven 4.0
- **Containerization**: Docker
- **API Documentation**: OpenAPI 3.1
- **Security**: Spring Security 7

### Infrastructure
- **Service Discovery**: Eureka Server
- **API Gateway**: Spring Cloud Gateway
- **Circuit Breaker**: Resilience4j
- **Message Broker**: RabbitMQ
- **Caching**: Redis
- **Monitoring**: Micrometer + Prometheus

## Microservice Architecture

### 1. API Gateway Service
- **Port**: 8080
- **Purpose**: Single entry point, routing, authentication
- **Key Features**:
  - Request routing based on path patterns
  - Rate limiting
  - Load balancing
  - Security filter chain

### 2. User Service
- **Port**: 8081
- **Database**: `users_db`
- **Purpose**: User authentication, authorization, profile management
- **Endpoints**:
  - `POST /api/users/register`
  - `POST /api/users/login`
  - `GET /api/users/profile`
  - `PUT /api/users/profile`

### 3. Product Service
- **Port**: 8082
- **Database**: `products_db`
- **Purpose**: Product catalog management
- **Endpoints**:
  - `GET /api/products`
  - `POST /api/products`
  - `GET /api/products/{id}`
  - `PUT /api/products/{id}`

### 4. Order Service
- **Port**: 8083
- **Database**: `orders_db`
- **Purpose**: Order processing and management
- **Endpoints**:
  - `POST /api/orders`
  - `GET /api/orders/{userId}`
  - `GET /api/orders/{id}`
  - `PUT /api/orders/{id}/status`

### 5. Inventory Service
- **Port**: 8084
- **Database**: `inventory_db`
- **Purpose**: Stock management and tracking
- **Endpoints**:
  - `GET /api/inventory/{productId}`
  - `PUT /api/inventory/{productId}`
  - `POST /api/inventory/reserve`
  - `POST /api/inventory/release`

### 6. Notification Service
- **Port**: 8085
- **Database**: `notifications_db`
- **Purpose**: Email notifications
- **Features**:
  - Order confirmations
  - Shipping notifications
  - Stock alerts

## Database Schema

### Users Database
```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_addresses (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    street VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(50),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    is_default BOOLEAN DEFAULT false
);
```

### Products Database
```sql
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    parent_id INTEGER REFERENCES categories(id)
);

CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(19,2) NOT NULL,
    sku VARCHAR(100) UNIQUE NOT NULL,
    category_id INTEGER REFERENCES categories(id),
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE product_attributes (
    id SERIAL PRIMARY KEY,
    product_id INTEGER REFERENCES products(id),
    attribute_name VARCHAR(100),
    attribute_value VARCHAR(255)
);
```

### Orders Database
```sql
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    total_amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    shipping_address JSONB,
    billing_address JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES orders(id),
    product_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    price DECIMAL(19,2) NOT NULL
);
```

### Inventory Database
```sql
CREATE TABLE inventory (
    id SERIAL PRIMARY KEY,
    product_id INTEGER UNIQUE NOT NULL,
    quantity_available INTEGER NOT NULL DEFAULT 0,
    quantity_reserved INTEGER NOT NULL DEFAULT 0,
    reorder_level INTEGER DEFAULT 10,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE inventory_transactions (
    id SERIAL PRIMARY KEY,
    product_id INTEGER NOT NULL,
    transaction_type VARCHAR(20) NOT NULL, -- 'IN', 'OUT', 'RESERVE', 'RELEASE'
    quantity INTEGER NOT NULL,
    reference_id INTEGER, -- Order ID, etc.
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Project Structure

```
ecommerce-microservices/
├── api-gateway/
├── eureka-server/
├── user-service/
├── product-service/
├── order-service/
├── inventory-service/
├── notification-service/
├── docker-compose.yml
└── shared/
    ├── common-dto/
    ├── common-security/
    └── common-exceptions/
```

## Key Implementation Details

### 1. Configuration Management
```yaml
# application.yml for all services
spring:
  application:
    name: ${SERVICE_NAME:unknown}
  config:
    import: optional:configserver:http://localhost:8888
  datasource:
    url: jdbc:postgresql://localhost:5432/${DB_NAME}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:password}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
```

### 2. Service Communication
- **Synchronous Communication**: RestTemplate/WebClient with @LoadBalanced and RabbitMQ RPC
- **Asynchronous Communication**: RabbitMQ events for order processing
- **Circuit Breaker**: Resilience4j for fault tolerance

### 3. RabbitMQ Synchronous Communication (RPC Pattern)

```java
@Service
public class InventoryServiceClient {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public InventoryResponse checkInventory(Long productId) {
        // Create a unique correlation ID for the RPC call
        String correlationId = UUID.randomUUID().toString();

        // Prepare the request message
        InventoryRequest request = new InventoryRequest(productId);
        Message message = MessageBuilder
            .withBody(objectMapper.writeValueAsBytes(request))
            .setContentType("application/json")
            .setCorrelationId(correlationId)
            .setReplyTo("inventory.reply")
            .build();

        // Send the request and wait for response with timeout
        Message response = rabbitTemplate.sendAndReceive("inventory.rpc", message);

        if (response != null) {
            return objectMapper.readValue(response.getBody(), InventoryResponse.class);
        }

        throw new ServiceUnavailableException("Inventory service is not responding");
    }
}
```

### 4. RabbitMQ Asynchronous Configuration

```java
@Configuration
@EnableRabbit
public class RabbitMQConfig {

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange("order.exchange");
    }

    @Bean
    public Queue inventoryQueue() {
        return QueueBuilder.durable("inventory.queue").build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable("notification.queue").build();
    }

    @Bean
    public Binding inventoryBinding() {
        return BindingBuilder
            .bind(inventoryQueue())
            .to(orderExchange())
            .with("order.created");
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
            .bind(notificationQueue())
            .to(orderExchange())
            .with("order.completed");
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        template.setReplyTimeout(5000); // 5 seconds timeout for RPC
        return template;
    }
}
```

### 5. RabbitMQ Event-Driven Architecture

```java
@Component
public class OrderEventHandler {

    @RabbitListener(queues = "inventory.queue")
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Process inventory reservation
        inventoryService.reserveStock(event.getOrderItems());

        // Send inventory reserved event
        rabbitTemplate.convertAndSend("order.exchange", "inventory.reserved",
            new InventoryReservedEvent(event.getOrderId()));
    }

    @RabbitListener(queues = "notification.queue")
    public void handleOrderCompleted(OrderCompletedEvent event) {
        // Update order status
        orderService.updateOrderStatus(event.getOrderId(), "CONFIRMED");

        // Send notification
        notificationService.sendOrderConfirmation(event);

        // Publish order processed event
        rabbitTemplate.convertAndSend("order.exchange", "order.processed",
            new OrderProcessedEvent(event.getOrderId()));
    }

    @RabbitListener(queues = "payment.queue")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        // Confirm inventory reservation
        inventoryService.confirmReservation(event.getOrderId());

        // Update order status to PROCESSING
        orderService.updateOrderStatus(event.getOrderId(), "PROCESSING");
    }
}
```

### 6. Security Implementation
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtDecoder(jwtDecoder()))
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .build();
    }
}
```

### 4. Event-Driven Architecture
```java
@Component
public class OrderEventHandler {

    @KafkaListener(topics = "order.created")
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Process inventory reservation
        inventoryService.reserveStock(event.getOrderItems());

        // Send notification
        notificationService.sendOrderConfirmation(event);
    }

    @KafkaListener(topics = "payment.completed")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        // Update order status
        orderService.updateOrderStatus(event.getOrderId(), "CONFIRMED");

        // Reserve inventory
        inventoryService.confirmReservation(event.getOrderId());
    }
}
```

## API Gateway Configuration
```java
@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("user-service", r -> r
                .path("/api/users/**")
                .uri("lb://user-service"))
            .route("product-service", r -> r
                .path("/api/products/**")
                .uri("lb://product-service"))
            .route("order-service", r -> r
                .path("/api/orders/**")
                .uri("lb://order-service"))
            .route("inventory-service", r -> r
                .path("/api/inventory/**")
                .uri("lb://inventory-service"))
            .build();
    }
}
```

## Docker Compose Setup
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:17
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  rabbitmq:
    image: rabbitmq:3.12-management
    container_name: rabbitmq
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: password
    ports:
      - "5672:5672"     # AMQP port
      - "15672:15672"   # Management UI port
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "ping"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  postgres_data:
  rabbitmq_data:
```

## Performance Considerations

### 1. Database Optimization
- **Indexing Strategy**: Proper indexes on foreign keys and frequently queried columns
- **Connection Pooling**: HikariCP configuration for optimal connection management
- **Query Optimization**: Use of JPA projections and DTOs to reduce data transfer

### 2. Caching Strategy
- **Redis Caching**: Product catalog data, user sessions
- **Local Caching**: Spring Cache with Caffeine for frequently accessed data
- **Cache Invalidation**: Event-driven cache updates

### 3. RabbitMQ Performance Optimization
```yaml
# application.yml for RabbitMQ optimization
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: password
    virtual-host: /
    publisher-confirm-type: correlated
    publisher-returns: true
    template:
      retry:
        enabled: true
        initial-interval: 1000ms
        max-attempts: 3
        max-interval: 10000ms
        multiplier: 1.5
    listener:
      simple:
        concurrency: 3
        max-concurrency: 10
        prefetch: 1
        acknowledge-mode: manual
        retry:
          enabled: true
```

### 4. Scalability
- **Horizontal Scaling**: Stateless services with load balancing
- **Database Read Replicas**: For read-heavy operations
- **Message Queues**: RabbitMQ asynchronous processing for order fulfillment

## Monitoring and Observability

### 1. Health Checks
```java
@Component
public class CustomHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        // Custom health check logic
        return Health.up()
            .withDetail("database", "Available")
            .withDetail("external-service", "Available")
            .build();
    }
}
```

### 2. Metrics Collection
- **Micrometer Integration**: Custom metrics for business KPIs
- **Prometheus Export**: Metrics endpoint for monitoring
- **Distributed Tracing**: Spring Cloud Sleuth

### 3. RabbitMQ Monitoring
```java
@Component
public class RabbitMQHealthIndicator implements HealthIndicator {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public Health health() {
        try {
            // Check RabbitMQ connection
            rabbitTemplate.execute(channel -> {
                channel.queueDeclarePassive("health.check");
                return null;
            });

            return Health.up()
                .withDetail("rabbitmq", "Connected")
                .withDetail("exchange", "order.exchange")
                .withDetail("queues", Arrays.asList("inventory.queue", "notification.queue"))
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("rabbitmq", "Connection failed")
                .withException(e)
                .build();
        }
    }
}

// Custom RabbitMQ Metrics
@Component
public class RabbitMQMetrics {

    private final MeterRegistry meterRegistry;
    private final Counter messageCounter;
    private final Timer messageTimer;

    public RabbitMQMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.messageCounter = Counter.builder("rabbitmq.messages.published")
            .description("Total number of messages published")
            .register(meterRegistry);
        this.messageTimer = Timer.builder("rabbitmq.message.processing.time")
            .description("Time taken to process messages")
            .register(meterRegistry);
    }

    public void incrementPublishedMessages(String queue) {
        messageCounter.increment(Tags.of("queue", queue));
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }
}
```

### 4. Logging Strategy
```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp/>
                <logLevel/>
                <loggerName/>
                <message/>
                <mdc/>
                <stackTrace/>
            </providers>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>
```

## Deployment Strategies

### 1. Container Orchestration
- **Kubernetes**: For production deployment
- **Docker Compose**: For local development
- **CI/CD Pipeline**: GitHub Actions for automated builds and deployments

### 2. Configuration Management
- **Spring Cloud Config**: Centralized configuration
- **Environment Variables**: For sensitive data
- **Kubernetes Secrets**: For production secrets

### 3. Blue-Green Deployment
- **Zero Downtime**: Gradual traffic shifting
- **Rollback Strategy**: Quick rollback capability
- **Health Checks**: Ensuring service readiness

## Best Practices Implemented

### 1. Code Quality
- **Code Reviews**: Pull request process
- **Unit Testing**: JUnit 5 with Mockito
- **Integration Testing**: TestContainers for database testing
- **Contract Testing**: Pact for API contracts

### 2. Security
- **OWASP Guidelines**: Security best practices
- **Input Validation**: Bean Validation annotations
- **SQL Injection Prevention**: Parameterized queries
- **Rate Limiting**: API gateway configuration

### 3. Error Handling
```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            "RESOURCE_NOT_FOUND",
            ex.getMessage(),
            Instant.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex) {
        ErrorResponse error = new ErrorResponse(
            "BUSINESS_ERROR",
            ex.getMessage(),
            Instant.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
```

## Getting Started

### Prerequisites
- Java 25
- Maven 4.0+
- Docker & Docker Compose
- PostgreSQL 17
- Git

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd ecommerce-microservices
   ```

2. **Start infrastructure services**
   ```bash
   docker-compose up -d postgres redis rabbitmq
   ```

3. **Build all services**
   ```bash
   mvn clean install
   ```

4. **Start services**
   ```bash
   # Start Eureka Server
   cd eureka-server && mvn spring-boot:run

   # Start API Gateway
   cd api-gateway && mvn spring-boot:run

   # Start individual services in separate terminals
   cd user-service && mvn spring-boot:run
   cd product-service && mvn spring-boot:run
   cd order-service && mvn spring-boot:run
   cd inventory-service && mvn spring-boot:run
   cd notification-service && mvn spring-boot:run
   ```

5. **Access the application**
   - API Gateway: http://localhost:8080
   - Eureka Dashboard: http://localhost:8761
   - RabbitMQ Management UI: http://localhost:15672 (admin/password)
   - Product Catalog: http://localhost:8080/api/products
   - User Registration: http://localhost:8080/api/users/register

## Testing

### Running Tests
```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# All tests with coverage
mvn clean test jacoco:report
```

### API Testing with Postman
Import the provided Postman collection to test all endpoints:
- User Management
- Product Catalog
- Order Processing
- Inventory Management

## Production Considerations

### 1. High Availability
- **Service Replication**: Multiple instances per service
- **Database Clustering**: PostgreSQL streaming replication
- **Load Balancing**: Multiple API gateway instances

### 2. Disaster Recovery
- **Data Backups**: Regular database backups
- **Multi-Region Deployment**: Geographic distribution
- **Monitoring**: Alerting and health checks

### 3. Performance Tuning
- **JVM Optimization**: Heap size and GC tuning
- **Database Tuning**: Query optimization and indexing
- **Network Optimization**: Connection pooling and timeouts

## 📈 Project Improvements & Recommendations

### 🏗️ Architecture Enhancements

#### 1. Domain-Driven Design (DDD) Implementation
```java
// Define clear domain boundaries
public enum DomainContext {
    IDENTITY_MANAGEMENT,      // User, Authentication, Authorization
    CATALOG_MANAGEMENT,      // Products, Categories, Inventory
    ORDER_MANAGEMENT,        // Orders, Payments, Fulfillment
    CUSTOMER_RELATIONSHIP,   // Profiles, Preferences, Addresses
    ANALYTICS,               // Reports, Metrics, Business Intelligence
    NOTIFICATION,            // Email, SMS, Push notifications
}

// Strongly typed domain events
@Event
public class OrderPlacedEvent extends DomainEvent {
    private final OrderId orderId;
    private final CustomerId customerId;
    private final List<OrderItem> items;
    private final Money totalAmount;
}
```

#### 2. Saga Pattern for Distributed Transactions
```java
@Component
public class OrderSagaOrchestrator {

    @SagaOrchestrationStart
    public void startOrderSaga(OrderPlacedEvent event) {
        SagaManager.start(new OrderProcessingSaga(event))
            .step("validateInventory")
            .step("reserveInventory")
            .step("processPayment")
            .step("confirmOrder")
            .step("updateInventory")
            .step("sendNotifications")
            .compensation("releaseInventory")
            .compensation("refundPayment")
            .compensation("cancelOrder");
    }
}
```

#### 3. CQRS and Event Sourcing
```java
// Write Model (Commands)
@RestController
@RequestMapping("/api/v1/commands/orders")
public class OrderCommandController {
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid CreateOrderCommand command) {
        OrderId orderId = orderCommandHandler.handle(command);
        return ResponseEntity.accepted().body(new OrderResponse(orderId.getValue()));
    }
}

// Read Model (Queries)
@RestController
@RequestMapping("/api/v1/queries/orders")
public class OrderQueryController {
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderView> getOrder(@PathVariable String orderId) {
        OrderView orderView = orderQueryRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return ResponseEntity.ok(orderView);
    }
}
```

### 🔒 Security Enhancements

#### 1. Enhanced JWT Security Configuration
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class JwtSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**", "/actuator/health", "/api/v1/public/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                    .decoder(jwtDecoder())
                )
            )
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                .contentTypeOptions(HeadersConfigurer.ContentTypeOptionsConfig::and)
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
                )
            )
            .build();
    }
}
```

#### 2. Multi-Factor Authentication (MFA)
```java
@RestController
@RequestMapping("/api/v1/auth/mfa")
public class MfaController {

    @PostMapping("/setup")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MfaSetupResponse> setupMfa(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userService.findById(userPrincipal.getUserId());

        // Generate TOTP secret
        String secret = totpService.generateSecret();

        // Generate QR code
        String issuer = "E-Commerce Platform";
        String accountName = user.getEmail();
        String qrCodeUrl = totpService.getQrCodeUrl(issuer, accountName, secret);
        String qrCodeBase64 = qrCodeGenerator.generateBase64QrCode(qrCodeUrl);

        // Generate backup codes
        List<String> backupCodes = mfaService.generateBackupCodes(user.getUserId());

        return ResponseEntity.ok(MfaSetupResponse.builder()
            .secret(secret)
            .qrCode(qrCodeBase64)
            .backupCodes(backupCodes)
            .build());
    }
}
```

#### 3. Attribute-Based Access Control (ABAC)
```java
@Component
public class AbacPolicyEnforcer {

    public boolean evaluateAccess(String resource, String action, Authentication authentication, Object context) {
        // Get user details
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userService.findById(userPrincipal.getUserId());

        // Build policy request
        PolicyRequest policyRequest = PolicyRequest.builder()
            .subject(Subject.builder()
                .id(user.getUserId())
                .roles(user.getRoles())
                .attributes(user.getUserAttributes())
                .build())
            .resource(Resource.builder()
                .id(resource)
                .attributes(context)
                .build())
            .action(Action.builder().name(action).build())
            .environment(Environment.builder()
                .time(Instant.now())
                .ipAddress(getClientIp())
                .build())
            .build();

        return policyEngine.evaluate(policyRequest);
    }
}

// Example usage in controllers
@PreAuthorize("@abacPolicyEnforcer.canAccessOrder(#orderId, 'read', authentication)")
public ResponseEntity<OrderDetails> getOrder(@PathVariable String orderId) {
    OrderDetails order = orderService.getOrderDetails(orderId);
    return ResponseEntity.ok(order);
}
```

### ⚡ Performance Optimizations

#### 1. Multi-Level Caching Strategy
```java
@Component
public class MultiLevelCacheManager {

    private final Cache<String, Object> l1Cache; // Local cache (Caffeine)
    private final RedisTemplate<String, Object> l2Cache; // Redis cache

    public <T> Optional<T> get(String key, Class<T> type) {
        // Level 1: Local cache (fastest)
        T value = (T) l1Cache.getIfPresent(key);
        if (value != null) {
            return Optional.of(value);
        }

        // Level 2: Redis cache (distributed)
        value = (T) l2Cache.opsForValue().get(key);
        if (value != null) {
            l1Cache.put(key, value); // Populate L1 cache
            return Optional.of(value);
        }

        return Optional.empty();
    }
}
```

#### 2. Database Optimization
```yaml
# application.yml for database optimization
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
      max-lifetime: 1200000
      leak-detection-threshold: 60000

  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 50
          fetch_size: 100
        order_inserts: true
        order_updates: true
        batch_versioned_data: true
        connection:
          provider_disables_autocommit: true
        query:
          in_clause_parameter_padding: true
```

#### 3. Database Read Replicas
```java
@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public DataSource replicaDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public DataSource routingDataSource() {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("master", masterDataSource());
        dataSourceMap.put("replica", replicaDataSource());

        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(masterDataSource());
        return routingDataSource;
    }
}

// Annotation for read-only operations
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ReadOnly {}
```

### 📊 Advanced Monitoring

#### 1. Distributed Tracing with OpenTelemetry
```java
@Configuration
public class TracingConfig {

    @Bean
    public OpenTelemetry openTelemetry() {
        Resource resource = Resource.getDefault()
            .merge(Resource.builder()
                .put(ResourceAttributes.SERVICE_NAME, "ecommerce-service")
                .put(ResourceAttributes.SERVICE_VERSION, "1.0.0")
                .build());

        return OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .addSpanProcessor(BatchSpanProcessor.builder(
                        JaegerGrpcSpanExporter.builder()
                            .setEndpoint("http://jaeger:14250")
                            .build())
                        .build())
                    .setResource(resource)
                    .build())
            .build();
    }
}
```

#### 2. Custom Business Metrics
```java
@Component
public class BusinessMetricsCollector {

    private final Counter orderCounter;
    private final Timer orderProcessingTimer;
    private final Gauge inventoryGauge;

    public BusinessMetricsCollector(MeterRegistry meterRegistry) {
        this.orderCounter = Counter.builder("business.orders.created")
            .description("Total number of orders created")
            .register(meterRegistry);
        this.orderProcessingTimer = Timer.builder("business.orders.processing.time")
            .description("Time taken to process orders")
            .register(meterRegistry);
        this.inventoryGauge = Gauge.builder("business.inventory.items")
            .description("Number of items in inventory")
            .register(meterRegistry, this, BusinessMetricsCollector::getInventoryCount);
    }

    public void recordOrderCreated(Order order) {
        orderCounter.increment(
            Tags.of(
                "customer_type", order.getCustomer().getType(),
                "order_value", categorizeOrderValue(order.getTotal())
            )
        );
    }
}
```

### 🚀 Scalability Improvements

#### 1. Kubernetes Auto-Scaling
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: product-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: product-service
  minReplicas: 2
  maxReplicas: 20
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Pods
      pods:
        metric:
          name: http_requests_per_second
        target:
          type: AverageValue
          averageValue: "100"
```

#### 2. Database Sharding Strategy
```java
@Component
public class ShardingStrategy {

    public String determineShard(String tenantId, String tableName) {
        // Implement consistent hashing for tenant isolation
        int shardCount = getShardCount(tableName);
        int shardIndex = consistentHash(tenantId, shardCount);

        return String.format("%s_shard_%d", tableName, shardIndex);
    }

    @ShardingHint("product")
    public Product findProduct(String productId) {
        String shard = determineShard(extractTenantId(productId), "products");
        return productRepository.findByProductIdAndShard(productId, shard);
    }
}
```

## Implementation Roadmap

### 🎯 Phase 1: Foundation (0-3 months)
1. **Security Hardening**
   - Enhanced JWT security with RS256 signing and token blacklisting
   - Enhance API security policies with rate limiting and threat protection

2. **Performance Optimization**
   - Implement multi-level caching
   - Optimize database queries and indexing
   - Add connection pooling optimization

### 🏗️ Phase 2: Architecture Enhancement (3-6 months)
1. **Domain-Driven Design**
   - Implement bounded contexts
   - Add domain events architecture
   - Implement CQRS pattern

2. **Advanced Monitoring**
   - Implement distributed tracing
   - Add custom business metrics
   - Set up comprehensive alerting

### 🚀 Phase 3: Scalability & Resilience (6-9 months)
1. **Auto-Scaling**
   - Implement Kubernetes HPA
   - Add custom metrics for scaling
   - Implement database sharding

2. **Chaos Engineering**
   - Add failure injection capabilities
   - Implement circuit breakers
   - Add resilience patterns

### 📊 Phase 4: Analytics & Intelligence (9-12 months)
1. **Real-time Analytics**
   - Implement event stream processing
   - Add customer analytics dashboard
   - Implement recommendation engine

2. **Advanced CI/CD**
   - Implement blue-green deployments
   - Add canary deployments
   - Implement feature flags

## Future Enhancements

1. **Machine Learning Integration**: Product recommendations
2. **Advanced Analytics**: Real-time business intelligence
3. **Mobile API**: GraphQL endpoint for mobile apps
4. **Search Service**: Elasticsearch integration
5. **Payment Gateway**: Multiple payment provider integration
6. **Shipping Integration**: Third-party logistics providers

## Conclusion

This microservice architecture demonstrates a scalable, maintainable approach to building modern e-commerce applications. The use of Java 25 and Spring Boot 4.0.0 provides access to the latest features and performance improvements, while PostgreSQL 17 ensures reliable data management. The architecture supports horizontal scaling, fault tolerance, and continuous delivery practices.

The modular design allows for independent development and deployment of services, enabling teams to work autonomously and deliver features faster. The comprehensive testing strategy and observability features ensure reliability and maintainability in production environments.