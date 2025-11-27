# E-Commerce Microservice API Specification

## Overview

This document defines the RESTful API contracts and JWT authentication specifications for the e-commerce microservice architecture. All APIs follow REST principles and use JWT for authentication and authorization.

## Table of Contents

1. [General API Guidelines](#general-api-guidelines)
2. [Authentication & Authorization](#authentication--authorization)
3. [Common Response Format](#common-response-format)
4. [Error Handling](#error-handling)
5. [Rate Limiting](#rate-limiting)
6. [API Endpoints](#api-endpoints)
   - [Authentication Service](#authentication-service)
   - [User Service](#user-service)
   - [Product Service](#product-service)
   - [Order Service](#order-service)
   - [Inventory Service](#inventory-service)
   - [Notification Service](#notification-service)
7. [Data Models](#data-models)
8. [API Versioning](#api-versioning)
9. [Security Considerations](#security-considerations)
10. [Testing Guidelines](#testing-guidelines)

---

## General API Guidelines

### Base URL
- **Development**: `http://localhost:8080/api/v1`
- **Production**: `https://api.yourdomain.com/api/v1`

### HTTP Methods
| Method | Description | Example |
|--------|-------------|---------|
| `GET` | Retrieve resources | `GET /products` |
| `POST` | Create new resource | `POST /orders` |
| `PUT` | Update existing resource | `PUT /users/{id}` |
| `PATCH` | Partially update resource | `PATCH /orders/{id}` |
| `DELETE` | Delete resource | `DELETE /products/{id}` |

### Headers
All requests should include the following headers:

#### Required Headers
```http
Content-Type: application/json
Accept: application/json
```

#### Authentication Header (for protected endpoints)
```http
Authorization: Bearer <JWT_TOKEN>
```

#### Optional Headers
```http
X-Request-ID: <unique-request-identifier>
X-Client-Version: <client-version>
Accept-Language: en-US
```

### URL Conventions
- Use lowercase letters and hyphens for readability
- Use plural nouns for resource collections
- Use query parameters for filtering, sorting, and pagination
- API versioning via URL path: `/api/v1/`

---

## Authentication & Authorization

### JWT Authentication Flow

#### 1. User Registration
```http
POST /api/v1/auth/register
```

#### 2. User Login
```http
POST /api/v1/auth/login
```

#### 3. Token Refresh
```http
POST /api/v1/auth/refresh
```

### JWT Token Structure

#### Access Token
```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT",
    "kid": "2023-key-id"
  },
  "payload": {
    "sub": "user-uuid",
    "username": "john.doe",
    "email": "john@example.com",
    "roles": ["ROLE_USER", "ROLE_CUSTOMER"],
    "authorities": ["READ_PROFILE", "CREATE_ORDER"],
    "iat": 1704067200,
    "exp": 1704153600,
    "iss": "https://api.yourdomain.com",
    "aud": "ecommerce-client"
  }
}
```

#### Refresh Token
```json
{
  "sub": "user-uuid",
  "type": "refresh",
  "iat": 1704067200,
  "exp": 1706659200,
  "iss": "https://api.yourdomain.com",
  "aud": "ecommerce-client"
}
```

### Token Expiration
- **Access Token**: 15 minutes
- **Refresh Token**: 30 days
- **Password Reset Token**: 1 hour

### Security Headers
```http
WWW-Authenticate: Bearer realm="E-Commerce API"
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000; includeSubDomains
```

---

## Common Response Format

### Success Response
```json
{
  "success": true,
  "data": {
    // Response data
  },
  "metadata": {
    "timestamp": "2024-01-01T12:00:00Z",
    "requestId": "req_123456789",
    "version": "v1",
    "pagination": {
      "page": 1,
      "size": 20,
      "totalElements": 100,
      "totalPages": 5,
      "first": true,
      "last": false
    }
  }
}
```

### Error Response
```json
{
  "success": false,
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "Product with ID 123 not found",
    "details": [
      {
        "field": "productId",
        "rejectedValue": "123",
        "message": "Product does not exist"
      }
    ],
    "timestamp": "2024-01-01T12:00:00Z",
    "requestId": "req_123456789",
    "path": "/api/v1/products/123"
  },
  "metadata": {
    "timestamp": "2024-01-01T12:00:00Z",
    "requestId": "req_123456789",
    "version": "v1"
  }
}
```

---

## Error Handling

### HTTP Status Codes
| Status | Code | Description |
|--------|------|-------------|
| 200 | SUCCESS | Request successful |
| 201 | CREATED | Resource created successfully |
| 204 | NO_CONTENT | Resource deleted successfully |
| 400 | BAD_REQUEST | Invalid request data |
| 401 | UNAUTHORIZED | Authentication required |
| 403 | FORBIDDEN | Insufficient permissions |
| 404 | NOT_FOUND | Resource not found |
| 409 | CONFLICT | Resource conflict |
| 422 | UNPROCESSABLE_ENTITY | Validation failed |
| 429 | TOO_MANY_REQUESTS | Rate limit exceeded |
| 500 | INTERNAL_SERVER_ERROR | Server error |

### Error Codes
| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| `INVALID_CREDENTIALS` | 401 | Invalid username or password |
| `TOKEN_EXPIRED` | 401 | JWT token has expired |
| `TOKEN_INVALID` | 401 | Invalid JWT token |
| `ACCESS_DENIED` | 403 | Insufficient permissions |
| `RESOURCE_NOT_FOUND` | 404 | Requested resource not found |
| `VALIDATION_FAILED` | 422 | Request validation failed |
| `DUPLICATE_RESOURCE` | 409 | Resource already exists |
| `RATE_LIMIT_EXCEEDED` | 429 | Too many requests |
| `SERVICE_UNAVAILABLE` | 503 | Service temporarily unavailable |

---

## Rate Limiting

### Rate Limits
| Endpoint Type | Rate | Window |
|---------------|------|--------|
| Public endpoints | 100 requests | 15 minutes |
| Authenticated endpoints | 1000 requests | 15 minutes |
| Admin endpoints | 5000 requests | 15 minutes |

### Rate Limit Headers
```http
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1704067200
```

---

## API Endpoints

### Authentication Service

#### Register User
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "john.doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890"
}
```

**Response (201):**
```json
{
  "success": true,
  "data": {
    "userId": "user-uuid",
    "username": "john.doe",
    "email": "john@example.com",
    "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 900,
    "roles": ["ROLE_USER"]
  },
  "metadata": {
    "timestamp": "2024-01-01T12:00:00Z",
    "requestId": "req_123456789",
    "version": "v1"
  }
}
```

#### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "john.doe",
  "password": "SecurePass123!"
}
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "userId": "user-uuid",
    "username": "john.doe",
    "email": "john@example.com",
    "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 900,
    "roles": ["ROLE_USER", "ROLE_CUSTOMER"]
  },
  "metadata": {
    "timestamp": "2024-01-01T12:00:00Z",
    "requestId": "req_123456789",
    "version": "v1"
  }
}
```

#### Refresh Token
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Logout
```http
POST /api/v1/auth/logout
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### User Service

#### Get User Profile
```http
GET /api/v1/users/profile
Authorization: Bearer <ACCESS_TOKEN>
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "userId": "user-uuid",
    "username": "john.doe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+1234567890",
    "status": "ACTIVE",
    "createdAt": "2024-01-01T10:00:00Z",
    "updatedAt": "2024-01-01T11:30:00Z",
    "addresses": [
      {
        "id": "addr-uuid-1",
        "street": "123 Main St",
        "city": "New York",
        "state": "NY",
        "postalCode": "10001",
        "country": "USA",
        "isDefault": true
      }
    ],
    "preferences": {
      "language": "en-US",
      "currency": "USD",
      "timezone": "America/New_York",
      "emailNotifications": true,
      "smsNotifications": false
    }
  },
  "metadata": {
    "timestamp": "2024-01-01T12:00:00Z",
    "requestId": "req_123456789",
    "version": "v1"
  }
}
```

#### Update User Profile
```http
PUT /api/v1/users/profile
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Smith",
  "phone": "+1234567890",
  "preferences": {
    "language": "en-US",
    "currency": "USD",
    "timezone": "America/New_York",
    "emailNotifications": true,
    "smsNotifications": false
  }
}
```

#### Add User Address
```http
POST /api/v1/users/addresses
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json

{
  "street": "456 Oak Ave",
  "city": "Los Angeles",
  "state": "CA",
  "postalCode": "90001",
  "country": "USA",
  "isDefault": false
}
```

### Product Service

#### Get Products
```http
GET /api/v1/products?page=0&size=20&sort=name,asc&category=electronics&search=laptop
Authorization: Bearer <ACCESS_TOKEN>
```

**Query Parameters:**
- `page` (int, default: 0): Page number
- `size` (int, default: 20): Page size (max: 100)
- `sort` (string): Sorting field and direction (e.g., "price,desc")
- `category` (string): Filter by category
- `search` (string): Search in product name and description
- `minPrice` (decimal): Minimum price filter
- `maxPrice` (decimal): Maximum price filter
- `inStock` (boolean): Filter by availability

**Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "productId": "prod-uuid-1",
      "name": "Laptop Pro 15",
      "description": "High-performance laptop with 15-inch display",
      "sku": "LP-PRO-15-BLK",
      "price": 1299.99,
      "originalPrice": 1499.99,
      "currency": "USD",
      "category": {
        "id": "cat-uuid-1",
        "name": "Electronics",
        "parentId": null
      },
      "images": [
        {
          "url": "https://cdn.example.com/products/laptop1.jpg",
          "alt": "Laptop Pro 15 front view",
          "isPrimary": true
        }
      ],
      "attributes": [
        {
          "name": "processor",
          "value": "Intel Core i7"
        },
        {
          "name": "ram",
          "value": "16GB"
        },
        {
          "name": "storage",
          "value": "512GB SSD"
        }
      ],
      "inventory": {
        "quantity": 50,
        "inStock": true,
        "reserved": 5
      },
      "rating": 4.5,
      "reviewCount": 128,
      "createdAt": "2024-01-01T10:00:00Z",
      "updatedAt": "2024-01-01T11:00:00Z"
    }
  ],
  "metadata": {
    "timestamp": "2024-01-01T12:00:00Z",
    "requestId": "req_123456789",
    "version": "v1",
    "pagination": {
      "page": 0,
      "size": 20,
      "totalElements": 150,
      "totalPages": 8,
      "first": true,
      "last": false
    }
  }
}
```

#### Get Product Details
```http
GET /api/v1/products/{productId}
Authorization: Bearer <ACCESS_TOKEN>
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "productId": "prod-uuid-1",
    "name": "Laptop Pro 15",
    "description": "High-performance laptop with 15-inch display, perfect for professionals and creators.",
    "sku": "LP-PRO-15-BLK",
    "price": 1299.99,
    "originalPrice": 1499.99,
    "currency": "USD",
    "category": {
      "id": "cat-uuid-1",
      "name": "Electronics",
      "description": "Electronic devices and accessories"
    },
    "images": [
      {
        "url": "https://cdn.example.com/products/laptop1.jpg",
        "alt": "Laptop Pro 15 front view",
        "isPrimary": true
      },
      {
        "url": "https://cdn.example.com/products/laptop2.jpg",
        "alt": "Laptop Pro 15 side view",
        "isPrimary": false
      }
    ],
    "attributes": [
      {
        "name": "processor",
        "value": "Intel Core i7-11800H"
      },
      {
        "name": "ram",
        "value": "16GB DDR4"
      },
      {
        "name": "storage",
        "value": "512GB NVMe SSD"
      },
      {
        "name": "display",
        "value": "15.6\" Full HD (1920x1080)"
      },
      {
        "name": "graphics",
        "value": "NVIDIA GeForce RTX 3060"
      }
    ],
    "specifications": {
      "weight": "2.1 kg",
      "dimensions": "35.5 x 23.8 x 2.3 cm",
      "battery": "86Wh",
      "warranty": "2 years"
    },
    "inventory": {
      "quantity": 50,
      "inStock": true,
      "reserved": 5,
      "available": 45
    },
    "rating": 4.5,
    "reviewCount": 128,
    "createdAt": "2024-01-01T10:00:00Z",
    "updatedAt": "2024-01-01T11:00:00Z"
  },
  "metadata": {
    "timestamp": "2024-01-01T12:00:00Z",
    "requestId": "req_123456789",
    "version": "v1"
  }
}
```

### Order Service

#### Create Order
```http
POST /api/v1/orders
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json

{
  "items": [
    {
      "productId": "prod-uuid-1",
      "quantity": 1,
      "price": 1299.99
    },
    {
      "productId": "prod-uuid-2",
      "quantity": 2,
      "price": 49.99
    }
  ],
  "shippingAddress": {
    "street": "123 Main St",
    "city": "New York",
    "state": "NY",
    "postalCode": "10001",
    "country": "USA"
  },
  "billingAddress": {
    "street": "123 Main St",
    "city": "New York",
    "state": "NY",
    "postalCode": "10001",
    "country": "USA"
  },
  "paymentMethod": {
    "type": "CREDIT_CARD",
    "cardToken": "tok_123456789",
    "last4": "4242"
  },
  "notes": "Please deliver between 9 AM - 5 PM"
}
```

**Response (201):**
```json
{
  "success": true,
  "data": {
    "orderId": "order-uuid-1",
    "orderNumber": "ORD-2024-001234",
    "userId": "user-uuid",
    "status": "PENDING",
    "items": [
      {
        "productId": "prod-uuid-1",
        "productName": "Laptop Pro 15",
        "quantity": 1,
        "unitPrice": 1299.99,
        "totalPrice": 1299.99
      },
      {
        "productId": "prod-uuid-2",
        "productName": "Wireless Mouse",
        "quantity": 2,
        "unitPrice": 49.99,
        "totalPrice": 99.98
      }
    ],
    "subtotal": 1399.97,
    "tax": 139.99,
    "shipping": 9.99,
    "total": 1549.95,
    "currency": "USD",
    "shippingAddress": {
      "street": "123 Main St",
      "city": "New York",
      "state": "NY",
      "postalCode": "10001",
      "country": "USA"
    },
    "billingAddress": {
      "street": "123 Main St",
      "city": "New York",
      "state": "NY",
      "postalCode": "10001",
      "country": "USA"
    },
    "paymentMethod": {
      "type": "CREDIT_CARD",
      "last4": "4242"
    },
    "statusHistory": [
      {
        "status": "PENDING",
        "timestamp": "2024-01-01T12:00:00Z",
        "note": "Order created"
      }
    ],
    "createdAt": "2024-01-01T12:00:00Z",
    "updatedAt": "2024-01-01T12:00:00Z"
  },
  "metadata": {
    "timestamp": "2024-01-01T12:00:00Z",
    "requestId": "req_123456789",
    "version": "v1"
  }
}
```

#### Get User Orders
```http
GET /api/v1/orders?page=0&size=10&status=ACTIVE
Authorization: Bearer <ACCESS_TOKEN>
```

**Query Parameters:**
- `page` (int, default: 0): Page number
- `size` (int, default: 10): Page size
- `status` (string): Filter by order status
- `startDate` (date): Filter by start date
- `endDate` (date): Filter by end date

#### Get Order Details
```http
GET /api/v1/orders/{orderId}
Authorization: Bearer <ACCESS_TOKEN>
```

#### Cancel Order
```http
DELETE /api/v1/orders/{orderId}
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json

{
  "reason": "Customer request"
}
```

### Inventory Service

#### Check Product Availability
```http
GET /api/v1/inventory/products/{productId}/availability
Authorization: Bearer <ACCESS_TOKEN>
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "productId": "prod-uuid-1",
    "quantity": 50,
    "reserved": 5,
    "available": 45,
    "inStock": true,
    "reorderLevel": 10,
    "lastUpdated": "2024-01-01T11:45:00Z"
  },
  "metadata": {
    "timestamp": "2024-01-01T12:00:00Z",
    "requestId": "req_123456789",
    "version": "v1"
  }
}
```

#### Reserve Inventory
```http
POST /api/v1/inventory/reserve
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json

{
  "orderId": "order-uuid-1",
  "items": [
    {
      "productId": "prod-uuid-1",
      "quantity": 1
    },
    {
      "productId": "prod-uuid-2",
      "quantity": 2
    }
  ],
  "expiresAt": "2024-01-01T13:00:00Z"
}
```

### Notification Service

#### Get User Notifications
```http
GET /api/v1/notifications?page=0&size=20&read=false&type=ORDER
Authorization: Bearer <ACCESS_TOKEN>
```

**Query Parameters:**
- `page` (int, default: 0): Page number
- `size` (int, default: 20): Page size
- `read` (boolean): Filter by read status
- `type` (string): Filter by notification type

**Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": "notif-uuid-1",
      "userId": "user-uuid",
      "type": "ORDER_STATUS",
      "title": "Order Confirmed",
      "message": "Your order ORD-2024-001234 has been confirmed and is being processed.",
      "data": {
        "orderId": "order-uuid-1",
        "orderNumber": "ORD-2024-001234",
        "status": "CONFIRMED"
      },
      "read": false,
      "createdAt": "2024-01-01T12:30:00Z"
    }
  ],
  "metadata": {
    "timestamp": "2024-01-01T12:00:00Z",
    "requestId": "req_123456789",
    "version": "v1",
    "pagination": {
      "page": 0,
      "size": 20,
      "totalElements": 5,
      "totalPages": 1,
      "first": true,
      "last": true
    }
  }
}
```

#### Mark Notification as Read
```http
PUT /api/v1/notifications/{notificationId}/read
Authorization: Bearer <ACCESS_TOKEN>
```

#### Update Notification Preferences
```http
PUT /api/v1/notifications/preferences
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json

{
  "emailNotifications": {
    "orderStatus": true,
    "promotions": false,
    "security": true,
    "recommendations": true
  },
  "smsNotifications": {
    "orderStatus": false,
    "promotions": false,
    "security": true,
    "recommendations": false
  },
  "pushNotifications": {
    "orderStatus": true,
    "promotions": true,
    "security": true,
    "recommendations": false
  }
}
```

---

## Data Models

### User Models

#### UserRegistrationRequest
```json
{
  "username": "john.doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890"
}
```

#### User
```json
{
  "userId": "user-uuid",
  "username": "john.doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "status": "ACTIVE",
  "createdAt": "2024-01-01T10:00:00Z",
  "updatedAt": "2024-01-01T11:30:00Z"
}
```

#### Address
```json
{
  "id": "addr-uuid-1",
  "street": "123 Main St",
  "city": "New York",
  "state": "NY",
  "postalCode": "10001",
  "country": "USA",
  "isDefault": true
}
```

### Product Models

#### Product
```json
{
  "productId": "prod-uuid-1",
  "name": "Laptop Pro 15",
  "description": "High-performance laptop",
  "sku": "LP-PRO-15-BLK",
  "price": 1299.99,
  "originalPrice": 1499.99,
  "currency": "USD",
  "category": {
    "id": "cat-uuid-1",
    "name": "Electronics"
  },
  "images": [
    {
      "url": "https://cdn.example.com/products/laptop1.jpg",
      "alt": "Laptop Pro 15 front view",
      "isPrimary": true
    }
  ],
  "attributes": [
    {
      "name": "processor",
      "value": "Intel Core i7"
    }
  ],
  "inventory": {
    "quantity": 50,
    "inStock": true,
    "reserved": 5
  },
  "rating": 4.5,
  "reviewCount": 128
}
```

### Order Models

#### Order
```json
{
  "orderId": "order-uuid-1",
  "orderNumber": "ORD-2024-001234",
  "userId": "user-uuid",
  "status": "PENDING",
  "items": [
    {
      "productId": "prod-uuid-1",
      "productName": "Laptop Pro 15",
      "quantity": 1,
      "unitPrice": 1299.99,
      "totalPrice": 1299.99
    }
  ],
  "subtotal": 1399.97,
  "tax": 139.99,
  "shipping": 9.99,
  "total": 1549.95,
  "currency": "USD",
  "shippingAddress": {
    "street": "123 Main St",
    "city": "New York",
    "state": "NY",
    "postalCode": "10001",
    "country": "USA"
  },
  "statusHistory": [
    {
      "status": "PENDING",
      "timestamp": "2024-01-01T12:00:00Z",
      "note": "Order created"
    }
  ],
  "createdAt": "2024-01-01T12:00:00Z",
  "updatedAt": "2024-01-01T12:00:00Z"
}
```

### Validation Rules

#### User Registration Validation
```json
{
  "username": {
    "type": "string",
    "minLength": 3,
    "maxLength": 50,
    "pattern": "^[a-zA-Z0-9._-]+$",
    "required": true
  },
  "email": {
    "type": "string",
    "format": "email",
    "maxLength": 100,
    "required": true
  },
  "password": {
    "type": "string",
    "minLength": 8,
    "maxLength": 128,
    "pattern": "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]",
    "required": true
  },
  "firstName": {
    "type": "string",
    "minLength": 1,
    "maxLength": 50,
    "required": true
  },
  "lastName": {
    "type": "string",
    "minLength": 1,
    "maxLength": 50,
    "required": true
  },
  "phone": {
    "type": "string",
    "pattern": "^\\+[1-9]\\d{1,14}$",
    "required": false
  }
}
```

#### Order Creation Validation
```json
{
  "items": {
    "type": "array",
    "minItems": 1,
    "maxItems": 50,
    "items": {
      "type": "object",
      "properties": {
        "productId": {
          "type": "string",
          "format": "uuid",
          "required": true
        },
        "quantity": {
          "type": "integer",
          "minimum": 1,
          "maximum": 10,
          "required": true
        },
        "price": {
          "type": "number",
          "minimum": 0.01,
          "maximum": 999999.99,
          "required": true
        }
      }
    }
  }
}
```

---

## API Versioning

### Versioning Strategy
- URL path versioning: `/api/v1/`, `/api/v2/`
- Backward compatibility maintained for at least 6 months
- Deprecation notices sent via response headers

### Version Response Headers
```http
API-Version: v1
API-Deprecated: false
API-Sunset: Tue, 01 Jan 2025 00:00:00 GMT
```

### Migration Examples
```http
# v1 (current)
GET /api/v1/products

# v2 (future)
GET /api/v2/products
```

---

## Security Considerations

### Authentication
- **JWT RS256** with asymmetric keys
- **Key Rotation**: Every 90 days
- **Token Blacklisting**: Redis-based with TTL
- **Rate Limiting**: IP-based and user-based limits

### Authorization
- **Role-Based Access Control (RBAC)**
- **Resource-based permissions**
- **Attribute-Based Access Control (ABAC)** for sensitive operations

### Input Validation
- **Strict JSON Schema validation**
- **SQL Injection prevention**
- **XSS protection** with input sanitization
- **CSRF protection** for state-changing operations

### Data Protection
- **Encryption at rest**: AES-256
- **Encryption in transit**: TLS 1.3
- **PII masking** in logs and responses
- **Data retention policies**

### Security Headers
```http
# All responses
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000; includeSubDomains
Content-Security-Policy: default-src 'self'
```

---

## Testing Guidelines

### Unit Testing
```bash
# Run unit tests
mvn test

# Run with coverage
mvn clean test jacoco:report
```

### Integration Testing
```bash
# Run integration tests
mvn verify -P integration-tests
```

### Contract Testing
```bash
# Run Pact tests
mvn pact:verify
```

### API Testing Examples

#### Authentication Test
```bash
# Register user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "TestPass123!",
    "firstName": "Test",
    "lastName": "User"
  }'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "TestPass123!"
  }'
```

#### Product API Test
```bash
# Get products
curl -X GET "http://localhost:8080/api/v1/products?page=0&size=10" \
  -H "Authorization: Bearer <TOKEN>"

# Get product details
curl -X GET "http://localhost:8080/api/v1/products/prod-uuid-1" \
  -H "Authorization: Bearer <TOKEN>"
```

### Postman Collection
The API includes a comprehensive Postman collection with:
- All endpoints pre-configured
- Environment variables for different environments
- Automated test scripts for response validation
- Authentication helpers for JWT management

### Load Testing
```bash
# Using JMeter
jmeter -n -t load-test.jmx -l results.jtl

# Using k6
k6 run --vus 100 --duration 60s load-test.js
```

---

## Appendix

### HTTP Status Code Reference
| Status | Meaning | When to Use |
|--------|---------|-------------|
| 200 OK | Success | GET requests successful |
| 201 Created | Created | POST requests successful |
| 204 No Content | No Content | DELETE requests successful |
| 400 Bad Request | Bad Request | Invalid request data |
| 401 Unauthorized | Unauthorized | No authentication or invalid token |
| 403 Forbidden | Forbidden | Insufficient permissions |
| 404 Not Found | Not Found | Resource doesn't exist |
| 409 Conflict | Conflict | Resource already exists |
| 422 Unprocessable Entity | Validation Error | Request validation failed |
| 429 Too Many Requests | Rate Limit | Request limit exceeded |
| 500 Internal Server Error | Server Error | Unexpected server error |
| 503 Service Unavailable | Service Down | Service temporarily unavailable |

### Error Code Reference
| Code | Category | HTTP Status |
|------|----------|-------------|
| `AUTH_*` | Authentication | 401, 403 |
| `VAL_*` | Validation | 400, 422 |
| `RES_*` | Resource | 404, 409 |
| `SYS_*` | System | 500, 503 |
| `BIZ_*` | Business Logic | 400, 409 |
| `SEC_*` | Security | 401, 403, 429 |

### JWT Claims Reference
| Claim | Type | Description |
|-------|------|-------------|
| `sub` | String | Subject (user ID) |
| `iss` | String | Issuer |
| `aud` | String | Audience |
| `exp` | Number | Expiration time |
| `iat` | Number | Issued at time |
| `jti` | String | JWT ID |
| `roles` | Array | User roles |
| `authorities` | Array | User permissions |
| `username` | String | Username |
| `email` | String | User email |

---

## 🔐 Enhanced Security Implementation

### Advanced JWT Configuration

#### JWT Security Headers
```http
# All API responses include security headers
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000; includeSubDomains
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'
```

#### Enhanced JWT Claims Structure
```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT",
    "kid": "2023-key-id"
  },
  "payload": {
    "sub": "user-uuid",
    "username": "john.doe",
    "email": "john@example.com",
    "roles": ["ROLE_USER", "ROLE_CUSTOMER"],
    "authorities": ["READ_PROFILE", "CREATE_ORDER", "READ_ORDERS"],
    "sessionId": "session-uuid",
    "iat": 1704067200,
    "exp": 1704153600,
    "iss": "https://api.yourdomain.com",
    "aud": "ecommerce-client",
    "jti": "jwt-uuid"
  }
}
```

#### JWT Token Management Endpoints

##### Refresh Token
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900
  },
  "metadata": {
    "timestamp": "2024-01-01T12:00:00Z",
    "requestId": "req_123456789",
    "version": "v1"
  }
}
```

##### Logout (Revoke Token)
```http
POST /api/v1/auth/logout
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Multi-Factor Authentication (MFA)

#### Setup MFA
```http
POST /api/v1/auth/mfa/setup
Authorization: Bearer <ACCESS_TOKEN>
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "secret": "JBSWY3DPEHPK3PXP",
    "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
    "manualEntryKey": "otpauth://totp/E-Commerce Platform:john@example.com?secret=JBSWY3DPEHPK3PXP&issuer=E-Commerce%20Platform",
    "backupCodes": [
      "12345678",
      "87654321",
      "11111111",
      "22222222",
      "33333333"
    ],
    "instructions": {
      "step1": "Scan the QR code with your authenticator app",
      "step2": "Enter the 6-digit code to verify",
      "step3": "Save your backup codes securely"
    }
  },
  "metadata": {
    "timestamp": "2024-01-01T12:00:00Z",
    "requestId": "req_123456789",
    "version": "v1"
  }
}
```

#### Verify MFA
```http
POST /api/v1/auth/mfa/verify
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json

{
  "code": "123456"
}
```

#### Disable MFA
```http
POST /api/v1/auth/mfa/disable
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json

{
  "password": "currentPassword",
  "totpCode": "123456"
}
```

### API Security Policies

#### Rate Limiting
```http
# Rate limiting headers included in all responses
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1704067200
X-RateLimit-Retry-After: 60
```

#### Rate Limit Categories
| Endpoint Category | Rate Limit | Window | Burst |
|------------------|-------------|---------|-------|
| Authentication | 5 requests | 15 minutes | 2 |
| Public APIs | 100 requests | 15 minutes | 10 |
| Authenticated | 1000 requests | 15 minutes | 50 |
| Admin APIs | 5000 requests | 15 minutes | 100 |

#### Security Event Logging
```http
# Security events are logged for monitoring
POST /api/v1/security/events
Authorization: Bearer <INTERNAL_SERVICE_TOKEN>
Content-Type: application/json

{
  "eventType": "LOGIN_SUCCESS",
  "userId": "user-uuid",
  "ipAddress": "192.168.1.1",
  "userAgent": "Mozilla/5.0...",
  "timestamp": "2024-01-01T12:00:00Z",
  "additionalInfo": {
    "mfaUsed": true,
    "loginMethod": "password_totp"
  }
}
```

---

## 📊 Advanced Monitoring & Observability

### Health Check Enhancements

#### Comprehensive Health Check
```http
GET /api/v1/actuator/health
```

**Response (200):**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "7.0.0"
      }
    },
    "rabbitmq": {
      "status": "UP",
      "details": {
        "exchange": "order.exchange",
        "queues": ["inventory.queue", "notification.queue"]
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 250685575168,
        "free": 67921432576,
        "threshold": 10737418240
      }
    },
    "ping": {
      "status": "UP"
    }
  },
  "groups": [
    "liveness",
    "readiness"
  ]
}
```

### Custom Business Metrics

#### Metrics Endpoints
```http
GET /api/v1/metrics/business
Authorization: Bearer <ADMIN_TOKEN>
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "orders": {
      "total": 15420,
      "today": 234,
      "thisWeek": 1456,
      "thisMonth": 6789,
      "averageOrderValue": 156.78,
      "conversionRate": 3.45
    },
    "users": {
      "total": 45678,
      "active": 3421,
      "newToday": 89,
      "newThisWeek": 456
    },
    "products": {
      "total": 8976,
      "inStock": 7234,
      "lowStock": 234,
      "outOfStock": 1508
    },
    "performance": {
      "averageResponseTime": 145,
      "p95ResponseTime": 289,
      "errorRate": 0.12,
      "uptime": 99.98
    }
  },
  "metadata": {
    "timestamp": "2024-01-01T12:00:00Z",
    "requestId": "req_123456789",
    "version": "v1"
  }
}
```

### Distributed Tracing

#### Trace Headers
```http
# Request tracing headers
X-Trace-Id: trace-uuid-123456789
X-Parent-Span-Id: parent-span-uuid
X-Span-Id: span-uuid-987654321
X-Sampled: true
```

#### Trace Information Response
```http
GET /api/v1/actuator/traces
Authorization: Bearer <ADMIN_TOKEN>
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "traces": [
      {
        "traceId": "trace-uuid-123456789",
        "spans": [
          {
            "spanId": "span-uuid-987654321",
            "parentSpanId": "parent-span-uuid",
            "operationName": "GET /api/v1/products",
            "startTime": 1704067200000000,
            "duration": 145000,
            "tags": {
              "http.method": "GET",
              "http.url": "/api/v1/products",
              "http.status_code": "200",
              "user_id": "user-uuid",
              "service.name": "product-service"
            },
            "logs": [
              {
                "timestamp": 1704067200000000,
                "level": "INFO",
                "message": "Request started"
              }
            ]
          }
        ]
      }
    ]
  }
}
```

---

## 🚀 Performance & Scalability Features

### Caching Strategy

#### Cache-Control Headers
```http
# Cache headers for GET requests
Cache-Control: public, max-age=300
ETag: "abc123def456"
Last-Modified: Wed, 01 Jan 2024 12:00:00 GMT
Vary: Accept-Encoding, Authorization
```

#### Cache Invalidation Webhook
```http
POST /api/v1/webhooks/cache-invalidate
Content-Type: application/json
Authorization: Bearer <INTERNAL_SERVICE_TOKEN>

{
  "eventType": "PRODUCT_UPDATED",
  "resourceId": "prod-uuid-123",
  "cacheKeys": [
    "products:page:0",
    "products:detail:prod-uuid-123",
    "products:category:cat-uuid-456"
  ]
}
```

### Database Performance

#### Query Performance Endpoint
```http
GET /api/v1/actuator/db/query-stats
Authorization: Bearer <ADMIN_TOKEN>
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "connectionPool": {
      "active": 5,
      "idle": 15,
      "total": 20,
      "max": 20,
      "waiting": 0
    },
    "slowQueries": [
      {
        "query": "SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC",
        "averageTime": 145,
        "maxTime": 567,
        "callCount": 1234,
        "totalTime": 178930
      }
    ],
    "tableStatistics": [
      {
        "tableName": "orders",
        "rowCount": 15420,
        "size": "245.6 MB",
        "indexUsage": 89.5
      }
    ]
  }
}
```

---

## 🧪 Advanced Testing Guidelines

### Load Testing Scenarios

#### Concurrent User Load Test
```bash
# Using k6 for load testing
k6 run --vus 1000 --duration 10m load-test.js
```

#### Load Test Script Example
```javascript
// load-test.js
import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  stages: [
    { duration: '2m', target: 100 },   // Ramp up to 100 users
    { duration: '5m', target: 100 },   // Stay at 100 users
    { duration: '2m', target: 500 },   // Ramp up to 500 users
    { duration: '5m', target: 500 },   // Stay at 500 users
    { duration: '2m', target: 1000 },  // Ramp up to 1000 users
    { duration: '5m', target: 1000 },  // Stay at 1000 users
    { duration: '2m', target: 0 },     // Ramp down to 0 users
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],    // 95% of requests < 500ms
    http_req_failed: ['rate<0.1'],      // Error rate < 0.1%
    http_reqs: ['rate>100'],           // Throughput > 100 req/s
  },
};

export default function () {
  let authResponse = http.post('http://localhost:8080/api/v1/auth/login', JSON.stringify({
    username: 'testuser',
    password: 'TestPass123!'
  }), {
    headers: { 'Content-Type': 'application/json' }
  });

  check(authResponse, {
    'login successful': (r) => r.status === 200,
    'token received': (r) => r.json('data.accessToken') !== undefined
  });

  let token = authResponse.json('data.accessToken');

  // Test product browsing
  let productsResponse = http.get('http://localhost:8080/api/v1/products?page=0&size=20', {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Accept': 'application/json'
    }
  });

  check(productsResponse, {
    'products loaded': (r) => r.status === 200,
    'products count > 0': (r) => r.json('data').length > 0
  });

  sleep(1);
}
```

### Contract Testing

#### Consumer Contract Test Example
```java
@Pact(provider = "product-service", consumer = "order-service")
public class ProductServicePactTest {

    @State("product exists")
    public void productExists() {
        testDatabase.addProduct(createTestProduct());
    }

    @Pact(consumer = "order-service")
    public RequestResponsePact getProductPact(PactDslWithProvider builder) {
        return builder
            .given("product exists")
            .uponReceiving("Get product by ID")
                .path("/api/v1/products/prod-123")
                .method("GET")
                .headers("Authorization", "Bearer token")
            .willRespondWith()
                .status(200)
                .headers("Content-Type", "application/json")
                .body(LambdaDsl.newJsonBody(body -> {
                    body.stringType("productId", "prod-123");
                    body.stringType("name", "Test Product");
                    body.numberType("price", 99.99);
                    body.booleanType("inStock", true);
                    body.array("attributes", array -> array.object(attr -> {
                        attr.stringType("name", "color");
                        attr.stringType("value", "black");
                    }));
                }).build())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getProductPact")
    public void testGetProduct(MockServer mockServer) {
        ProductClient client = new ProductClient(mockServer.getUrl());
        Product product = client.getProduct("prod-123");

        assertThat(product.getProductId()).isEqualTo("prod-123");
        assertThat(product.getPrice()).isEqualTo(99.99);
        assertThat(product.isInStock()).isTrue();
    }
}
```

### Chaos Engineering

#### Fault Injection API
```http
POST /api/v1/admin/chaos/inject-fault
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
  "targetService": "product-service",
  "faultType": "latency",
  "parameters": {
    "duration": 30000,
    "latencyMs": 2000,
    "affectedEndpoints": ["/api/v1/products"],
    "faultPercentage": 50
  }
}
```

#### Circuit Breaker Status
```http
GET /api/v1/actuator/circuitbreakers
Authorization: Bearer <ADMIN_TOKEN>
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "circuitBreakers": [
      {
        "name": "inventoryService",
        "state": "CLOSED",
        "failureRate": 0.05,
        "slowCallRate": 0.02,
        "bufferedCalls": 100,
        "failedCalls": 5,
        "slowCalls": 2,
        "notPermittedCalls": 0
      },
      {
        "name": "paymentService",
        "state": "OPEN",
        "failureRate": 0.75,
        "slowCallRate": 0.10,
        "bufferedCalls": 50,
        "failedCalls": 37,
        "slowCalls": 5,
        "notPermittedCalls": 25
      }
    ]
  }
}
```

---

## 📋 Implementation Checklists

### Security Checklist
- [ ] Enhanced JWT security with RS256 signing and token validation
- [ ] Multi-factor authentication (TOTP/backup codes)
- [ ] JWT token blacklisting and session management
- [ ] API security policies and threat protection
- [ ] Security headers implementation
- [ ] Regular security audits

### Performance Checklist
- [ ] Multi-level caching implementation
- [ ] Database optimization
- [ ] Connection pooling tuning
- [ ] Load testing completion
- [ ] Performance monitoring setup

### Scalability Checklist
- [ ] Auto-scaling configuration
- [ ] Database sharding strategy
- [ ] Read-replica implementation
- [ ] Load balancing optimization
- [ ] Resource monitoring

### Observability Checklist
- [ ] Distributed tracing implementation
- [ ] Custom metrics collection
- [ ] Alerting rules configuration
- [ ] Log aggregation setup
- [ ] Dashboard creation

### Testing Checklist
- [ ] Unit testing coverage > 80%
- [ ] Integration testing setup
- [ ] Contract testing implementation
- [ ] Load testing scenarios
- [ ] Chaos engineering practices

---

### Schema Definitions

All API schemas are available at:
- **OpenAPI 3.1**: `/api/v1/docs/openapi.json`
- **Swagger UI**: `/api/v1/docs/swagger-ui.html`
- **ReDoc**: `/api/v1/docs/redoc.html`

### Additional Documentation

- **API Health Checks**: `/api/v1/actuator/health`
- **Metrics Dashboard**: `/api/v1/actuator/metrics`
- **Business Metrics**: `/api/v1/metrics/business`
- **Tracing Information**: `/api/v1/actuator/traces`
- **Load Testing Scripts**: `/docs/load-testing/`
- **Contract Tests**: `/docs/pact/`

This comprehensive specification document serves as the authoritative contract for all client integrations and should be kept in sync with the actual API implementation. Any breaking changes must be versioned according to the versioning strategy outlined above.

The document includes advanced security features, performance optimizations, monitoring capabilities, and testing guidelines to ensure enterprise-grade reliability and maintainability.