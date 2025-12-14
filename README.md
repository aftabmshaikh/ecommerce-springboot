# 🛍️ E-Commerce Microservices Platform

A modern, scalable, and cloud-native e-commerce platform built with Spring Boot microservices, deployed with Docker and Kubernetes. This project demonstrates a full-stack microservices architecture with best practices in distributed systems, containerization, and cloud deployment.

## 🚀 Features

- **Modular Architecture**: Independently deployable microservices
- **Containerized**: Docker support for all services
- **Orchestration**: Kubernetes deployment ready
- **CI/CD**: GitHub Actions for automated testing and deployment
- **Monitoring**: Integrated with Prometheus, Grafana, and ELK Stack
- **Security**: JWT Authentication with Keycloak
- **Event-Driven**: Asynchronous communication using Apache Kafka
- **Resilient**: Circuit breakers, retries, and fallbacks with Resilience4j
- **API Documentation**: Swagger/OpenAPI for all services

## Table of Contents
- [Microservices Overview](#microservices-overview)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Testing APIs](#testing-apis)
- [Sample Data](#sample-data)
- [Monitoring and Logging](#monitoring-and-logging)
- [Troubleshooting](#troubleshooting)
- [License](#license)

## Microservices Overview

| Service | Port | Description |
|---------|------|-------------|
| API Gateway | 8080 | Entry point for all client requests |
| Service Discovery | 8765 | Eureka server for service registration and discovery |
| Config Server | 8888 | Centralized configuration management |
| User Service | 8081 | User management and authentication |
| Product Service | 8082 | Product catalog and inventory management |
| Order Service | 8083 | Order processing and management |
| Payment Service | 8084 | Payment processing |
| Notification Service | 8085 | Email and notification services |
| Keycloak | 8086 | Identity and Access Management |
| PostgreSQL | 5432 | Primary database |
| Redis | 6379 | Caching service |
| Kafka | 9092 | Message broker for event-driven architecture |
| Zipkin | 9411 | Distributed tracing |
| Prometheus | 9090 | Metrics collection |
| Grafana | 3001 | Metrics visualization |
| ELK Stack | 9200, 9600, 5601 | Logging and log analysis |
| Jaeger | 16686 | Distributed tracing UI |

## 🛠️ Tech Stack

### Backend (Microservices)
- **Spring Boot 3.x** - Core application framework
- **Spring Cloud** - Service discovery, configuration, API Gateway
- **Spring Data JPA** - Data access
- **Spring Security** - Authentication and authorization
- **Spring Cloud Stream** - Event-driven architecture
- **Resilience4j** - Circuit breaking and fault tolerance
- **Spring Cloud OpenFeign** - Service-to-service communication
- **Spring Cloud Config** - Centralized configuration
- **Lombok** - Boilerplate reduction
- **MapStruct** - Object mapping
- **JUnit 5** - Unit and integration testing
- **Testcontainers** - Integration testing with real dependencies

### Infrastructure
- **Docker** - Containerization
- **Docker Compose** - Multi-container applications
- **Kubernetes** - Container orchestration
- **Helm** - Kubernetes package manager
- **NGINX** - Reverse proxy and load balancing

### Database
- **PostgreSQL** - Primary database
- **MongoDB** - Document storage
- **Redis** - Caching and session management
- **Elasticsearch** - Search and analytics

### Message Broker
- **Apache Kafka** - Event streaming platform

### Monitoring & Observability
- **Prometheus** - Metrics collection
- **Grafana** - Metrics visualization
- **ELK Stack** - Logging and log analysis
- **Jaeger** - Distributed tracing
- **Spring Boot Actuator** - Application monitoring

### CI/CD
- **GitHub Actions** - CI/CD pipelines
- **ArgoCD** - GitOps tool for Kubernetes

### Security
- **Keycloak** - Identity and Access Management
- **JWT** - Stateless authentication
- **OAuth 2.0** - Authorization framework
- **Spring Security** - Authentication and access control

## 📋 Prerequisites

### Local Development
- **Docker** (v20.10+) and **Docker Compose** (v2.0+)
- **Java 17** or later
- **Maven** 3.6+ or **Gradle** 7.0+
- **Git**

### Kubernetes Deployment (Optional)
- **kubectl**
- **Minikube** (for local Kubernetes) or access to a Kubernetes cluster
- **Helm** (v3.0+)
- **Skaffold** (for development workflow)

### Recommended Tools
- **IntelliJ IDEA** or **VS Code**
- **Postman** or **Insomnia** for API testing
- **k9s** for Kubernetes management
- **Lens** for Kubernetes visualization

## 🚀 Quick Start

### Option 1: Docker Compose (Recommended for Development)

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/ecommerce-microservices.git
   cd ecommerce-microservices
   ```

2. **Set up environment variables**
   ```bash
   cp .env.example .env
   # Edit .env file with your configuration
   ```

3. **Start all services**
   ```bash
   docker-compose -f ecommerce-springboot/docker-compose-springboot.yml up -d
   ```
   This will start all required services including:
   - API Gateway
   - Service Discovery (Eureka)
   - Config Server
   - Microservices (User, Product, Order, etc.)
   - Databases (PostgreSQL, MongoDB, Redis)
   - Monitoring stack (Prometheus, Grafana, ELK)

4. **Verify services are running**
   ```bash
   docker-compose ps
   ```

5. **Access the application**
   - API Gateway: http://localhost:8080
   - Service Discovery: http://localhost:8765
   - Keycloak Admin Console: http://localhost:8081 (admin/admin)
   - Grafana: http://localhost:3001 (admin/admin)
   - Kibana: http://localhost:5601
   - Prometheus: http://localhost:9090
   - Jaeger: http://localhost:16686

### Option 2: Kubernetes (Production-like Environment)

1. **Start Minikube (for local development)**
   ```bash
   minikube start --cpus=4 --memory=8192mb --disk-size=40g
   minikube addons enable ingress
   minikube addons enable metrics-server
   ```

2. **Install Helm (if not already installed)**
   ```bash
   # For macOS
   brew install helm
   
   # For Linux
   curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
   ```

3. **Deploy the application**
   ```bash
   # Add required Helm repositories
   helm repo add bitnami https://charts.bitnami.com/bitnami
   helm repo add jetstack https://charts.jetstack.io
   helm repo update
   
   # Create namespace
   kubectl create namespace ecommerce
   
   # Install dependencies
   helm install kafka bitnami/kafka --namespace ecommerce
   helm install postgresql bitnami/postgresql --namespace ecommerce
   helm install redis bitnami/redis --namespace ecommerce
   
   # Deploy application
   kubectl apply -f k8s/
   ```

4. **Access the application**
   ```bash
   # Enable ingress
   minikube tunnel
   
   # Get application URLs
   minikube service list -n ecommerce
   ```

### Option 3: Development Mode with Skaffold

1. **Install Skaffold**
   ```bash
   # For macOS
   brew install skaffold
   
   # For Linux
   curl -Lo skaffold https://storage.googleapis.com/skaffold/releases/latest/skaffold-linux-amd64 && \
   sudo install skaffold /usr/local/bin/
   ```

2. **Start development environment**
   ```bash
   skaffold dev
   ```
   This will:
   - Build and deploy all services
   - Stream logs from all pods
   - Watch for code changes and rebuild/redeploy automatically
   - Port-forward services for local access

## Backend Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/ecommerce-microservices.git
   cd ecommerce-microservices
   ```

2. **Make scripts executable**
   ```bash
   chmod +x scripts/*.sh
   ```

3. **Start the application**
   ```bash
   # Using Docker Compose (simplest way to start)
   docker-compose -f ecommerce-springboot/docker-compose-springboot.yml up -d
   
   # Or using Kubernetes (more production-like)
   ./scripts/1-setup-local-env.sh
   ./scripts/build.sh
   ./scripts/deploy.sh
   ```

4. **Access the application**
   - API Gateway: http://localhost:8080
   - Eureka Dashboard: http://localhost:8765
   - Keycloak Admin: http://localhost:8086 (admin/admin)
   - Grafana: http://localhost:3001 (admin/admin)
   - Kibana: http://localhost:5601
   - Jaeger: http://localhost:16686

## API Documentation

### Authentication
- **Login**: `POST /api/auth/login`
- **Register**: `POST /api/auth/register`
- **Refresh Token**: `POST /api/auth/refresh-token`

### User Service
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `POST /api/users` - Create new user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Product Service
- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create new product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

### Order Service
- `GET /api/orders` - Get all orders
- `GET /api/orders/{id}` - Get order by ID
- `POST /api/orders` - Create new order
- `PUT /api/orders/{id}/status` - Update order status
- `DELETE /api/orders/{id}` - Cancel order

## Monitoring and Logging

### Prometheus
- URL: http://localhost:9090
- Scrapes metrics from all services
- Pre-configured with service discovery

### Grafana
- URL: http://localhost:3001
- Default credentials: admin/admin
- Pre-configured dashboards for:
  - Spring Boot metrics
  - JVM metrics
  - Database metrics
  - System metrics

### ELK Stack
- Elasticsearch: http://localhost:9200
- Logstash: http://localhost:9600
- Kibana: http://localhost:5601
- Centralized logging for all services

### Jaeger
- URL: http://localhost:16686
- Distributed tracing for microservices
- End-to-end request tracing

## Troubleshooting

### Common Issues

1. **Port conflicts**
   - Ensure no other services are running on the required ports
   - Check with `netstat -tuln | grep <port>` (Linux/Mac) or `netstat -ano | findstr :<port>` (Windows)

2. **Docker resource constraints**
   - Increase Docker memory allocation in Docker Desktop settings (recommended: 8GB+)
   - Add more CPU cores if possible

3. **Database connection issues**
   - Verify database credentials in `application.yml`
   - Check if database containers are running
   - Run database migrations if needed

4. **Service discovery issues**
   - Check Eureka dashboard at http://localhost:8765
   - Verify service names and ports in `bootstrap.yml`

5. **Kubernetes deployment issues**
   - Check pod status: `kubectl get pods -n ecommerce`
   - View logs: `kubectl logs -f <pod-name> -n ecommerce`
   - Describe pod: `kubectl describe pod <pod-name> -n ecommerce`

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [Spring Cloud](https://spring.io/projects/spring-cloud)
- [Docker](https://www.docker.com/)
- [Kubernetes](https://kubernetes.io/)
- [Prometheus](https://prometheus.io/)
- [Grafana](https://grafana.com/)
- [Keycloak](https://www.keycloak.org/)
