#!/bin/bash

# E-Commerce Infrastructure Startup Script
# This script starts all required infrastructure services

set -e

echo "🏗️  Starting E-Commerce Infrastructure..."
echo "================================"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Check if docker-compose is available
if ! command -v docker compose &> /dev/null 2>&1; then
    echo "❌ docker compose is not available. Please install docker compose first."
    exit 1
fi

# Create necessary directories
echo "📁 Creating necessary directories..."
mkdir -p logs
mkdir -p data/postgres
mkdir -p data/redis
mkdir -p data/rabbitmq
mkdir -p data/elasticsearch
mkdir -p data/prometheus
mkdir -p data/grafana

# Start core infrastructure services
echo "🚀 Starting core infrastructure services..."

# Start PostgreSQL
echo "   🐘 Starting PostgreSQL..."
docker compose up -d postgres

# Wait for PostgreSQL to be ready
echo "   ⏳ Waiting for PostgreSQL to be ready..."
sleep 10
until docker exec ecommerce-postgres pg_isready -U postgres > /dev/null 2>&1; do
    echo "   ⏳ PostgreSQL is not ready yet, waiting..."
    sleep 5
done

# Start Redis
echo "   🔴 Starting Redis..."
docker compose up -d redis

# Wait for Redis to be ready
echo "   ⏳ Waiting for Redis to be ready..."
sleep 5
until docker exec ecommerce-redis redis-cli ping > /dev/null 2>&1; do
    echo "   ⏳ Redis is not ready yet, waiting..."
    sleep 3
done

# Start RabbitMQ
echo "   🐰 Starting RabbitMQ..."
docker compose up -d rabbitmq

# Wait for RabbitMQ to be ready
echo "   ⏳ Waiting for RabbitMQ to be ready..."
sleep 10
until docker exec ecommerce-rabbitmq rabbitmq-diagnostics ping > /dev/null 2>&1; do
    echo "   ⏳ RabbitMQ is not ready yet, waiting..."
    sleep 5
done

# Initialize databases
echo "   🗄️ Initializing databases..."
docker exec ecommerce-postgres psql -U postgres -f /docker-entrypoint-initdb.d/init-databases.sql

echo "✅ Core infrastructure services are ready!"
echo ""

# Show service status
echo "📊 Service Status:"
echo "=================="
echo "PostgreSQL: http://localhost:5432"
echo "   - Users DB: users_db"
echo "   - Products DB: products_db"
echo "   - Orders DB: orders_db"
echo "   - Inventory DB: inventory_db"
echo "   - Notifications DB: notifications_db"
echo ""
echo "Redis: localhost:6379"
echo ""
echo "RabbitMQ Management UI: http://localhost:15672"
echo "   - Username: admin"
echo "   - Password: password"
echo ""

# Optional: Start search and monitoring services
if [ "$1" = "--with-search" ]; then
    echo "🔍 Starting search services..."
    docker compose --profile search up -d elasticsearch
    echo "   📚 Elasticsearch: http://localhost:9200"
fi

if [ "$1" = "--with-monitoring" ]; then
    echo "📊 Starting monitoring services..."
    docker compose --profile monitoring up -d prometheus grafana
    echo "   📈 Prometheus: http://localhost:9090"
    echo "   📊 Grafana: http://localhost:3000"
    echo "   - Username: admin"
    echo "   - Password: admin"
fi

if [ "$1" = "--with-tracing" ]; then
    echo "🔍 Starting tracing services..."
    docker compose --profile tracing up -d jaeger
    echo "   🔍 Jaeger UI: http://localhost:16686"
fi

if [ "$1" = "--all" ]; then
    echo "🚀 Starting all services..."
    docker compose --profile search --profile monitoring --profile tracing up -d
    echo "   📚 Elasticsearch: http://localhost:9200"
    echo "   📈 Prometheus: http://localhost:9090"
    echo "   📊 Grafana: http://localhost:3000"
    echo "   🔍 Jaeger UI: http://localhost:16686"
fi

echo ""
echo "🎉 Infrastructure is ready!"
echo ""
echo "Next steps:"
echo "1. Run 'mvn clean install' to build all modules"
echo "2. Start Eureka Server: cd eureka-server && mvn spring-boot:run"
echo "3. Start API Gateway: cd api-gateway && mvn spring-boot:run"
echo "4. Start other services as needed"
echo ""
echo "Useful commands:"
echo "- Stop infrastructure: docker compose down"
echo "- View logs: docker compose logs -f [service-name]"
echo "- Restart service: docker compose restart [service-name]"
echo "- Check service health: docker compose ps"