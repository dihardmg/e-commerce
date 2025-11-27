package com.ecommerce.test.util;

import com.ecommerce.dto.common.PaginationInfo;
import com.ecommerce.dto.common.CommonResponse;
import com.ecommerce.dto.common.ErrorDetail;
import com.ecommerce.dto.common.ErrorResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Test Data Factory
 *
 * Factory for creating test data
 */
public class TestDataFactory {

    /**
     * Create test user
     */
    public static com.ecommerce.dto.user.UserDTO createTestUser() {
        return com.ecommerce.dto.user.UserDTO.builder()
                .id(UUID.randomUUID().toString())
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("+1234567890")
                .active(true)
                .roles(List.of("ROLE_USER"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create test product
     */
    public static com.ecommerce.dto.product.ProductDTO createTestProduct() {
        return com.ecommerce.dto.product.ProductDTO.builder()
                .id(UUID.randomUUID().toString())
                .sku("TEST-SKU-001")
                .name("Test Product")
                .description("Test product description")
                .categoryId(UUID.randomUUID().toString())
                .brand("Test Brand")
                .price(new BigDecimal("99.99"))
                .currency("USD")
                .availableStock(100)
                .minimumStock(10)
                .maximumStock(1000)
                .weight(new BigDecimal("1.5"))
                .weightUnit("kg")
                .dimensions("10x20x5")
                .tags(List.of("test", "product"))
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create test order
     */
    public static com.ecommerce.dto.order.OrderDTO createTestOrder() {
        return com.ecommerce.dto.order.OrderDTO.builder()
                .id(UUID.randomUUID().toString())
                .orderNumber("ORD-" + System.currentTimeMillis())
                .customerId(UUID.randomUUID().toString())
                .customerEmail("customer@example.com")
                .orderStatus("PENDING")
                .orderDate(LocalDateTime.now())
                .expectedDeliveryDate(LocalDateTime.now().plusDays(5))
                .subtotal(new BigDecimal("100.00"))
                .taxAmount(new BigDecimal("10.00"))
                .shippingAmount(new BigDecimal("15.00"))
                .discountAmount(new BigDecimal("5.00"))
                .totalAmount(new BigDecimal("120.00"))
                .currency("USD")
                .paymentMethod("CREDIT_CARD")
                .paymentStatus("PENDING")
                .orderItems(List.of(createTestOrderItem()))
                .build();
    }

    /**
     * Create test order item
     */
    public static com.ecommerce.dto.order.OrderItemDTO createTestOrderItem() {
        return com.ecommerce.dto.order.OrderItemDTO.builder()
                .id(UUID.randomUUID().toString())
                .productId(UUID.randomUUID().toString())
                .sku("ITEM-SKU-001")
                .productName("Test Item")
                .unitPrice(new BigDecimal("25.00"))
                .quantity(4)
                .totalPrice(new BigDecimal("100.00"))
                .build();
    }

    /**
     * Create pagination info
     */
    public static PaginationInfo createPaginationInfo(int page, int size, long totalElements) {
        return PaginationInfo.builder()
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages((int) Math.ceil((double) totalElements / size))
                .first(page == 0)
                .last(page >= (int) Math.ceil((double) totalElements / size) - 1)
                .hasNext(page < (int) Math.ceil((double) totalElements / size) - 1)
                .hasPrevious(page > 0)
                .build();
    }

    /**
     * Create success response
     */
    public static <T> CommonResponse<T> createSuccessResponse(T data) {
        return CommonResponse.<T>builder()
                .success(true)
                .data(data)
                .message("Success")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create paginated success response
     */
    public static <T> CommonResponse<List<T>> createPaginatedResponse(List<T> data, PaginationInfo pagination) {
        return CommonResponse.<List<T>>builder()
                .success(true)
                .data(data)
                .message("Success")
                .pagination(pagination)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create error response
     */
    public static ErrorResponse createErrorResponse(String errorCode, String message) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create error response with details
     */
    public static ErrorResponse createErrorResponse(String errorCode, String message, List<ErrorDetail> errorDetails) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .errorDetails(errorDetails)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create error detail
     */
    public static ErrorDetail createErrorDetail(String field, String message, Object rejectedValue) {
        return ErrorDetail.builder()
                .field(field)
                .message(message)
                .rejectedValue(rejectedValue)
                .build();
    }

    /**
     * Create test event
     */
    public static com.ecommerce.event.BaseEvent createTestEvent() {
        return com.ecommerce.event.domain.user.UserRegisteredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .sourceService("test-service")
                .version("1.0")
                .correlationId(UUID.randomUUID().toString())
                .userId(UUID.randomUUID().toString())
                .userId("test-user-id")
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .roles(List.of("ROLE_USER"))
                .registrationSource("WEB")
                .emailVerificationRequired(true)
                .phoneVerificationRequired(false)
                .build();
    }

    /**
     * Create random string
     */
    public static String createRandomString(int length) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, Math.min(length, 32));
    }

    /**
     * Create random email
     */
    public static String createRandomEmail() {
        return "test-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    /**
     * Create random phone number
     */
    public static String createRandomPhoneNumber() {
        return "+1" + (1000000000L + (long) (Math.random() * 9000000000L));
    }

    /**
     * Create random UUID
     */
    public static String createRandomUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Create random BigDecimal
     */
    public static BigDecimal createRandomBigDecimal(BigDecimal min, BigDecimal max) {
        double randomValue = min.doubleValue() + Math.random() * (max.doubleValue() - min.doubleValue());
        return BigDecimal.valueOf(randomValue).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Create random integer
     */
    public static int createRandomInt(int min, int max) {
        return min + (int) (Math.random() * (max - min + 1));
    }

    /**
     * Create random long
     */
    public static long createRandomLong(long min, long max) {
        return min + (long) (Math.random() * (max - min + 1));
    }

    /**
     * Create current timestamp
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Create timestamp in future
     */
    public static LocalDateTime futureDays(int days) {
        return LocalDateTime.now().plusDays(days);
    }

    /**
     * Create timestamp in past
     */
    public static LocalDateTime pastDays(int days) {
        return LocalDateTime.now().minusDays(days);
    }
}