# IRCTC Microservices — Local Development Guide

A Spring Boot microservices application for train booking (IRCTC-style), built with Java 17, Spring Cloud, MySQL, Redis, and Apache Kafka, with integrated monitoring (Prometheus, Grafana, Zipkin, SonarQube).

---

## Architecture

```
                        ┌──────────────────┐
     Client              │   API Gateway    │  :8080
     ───────────────────▶│  (Spring Cloud)  │
                        └────────┬─────────┘
                                 │ routes via Eureka
         ┌───────────────────────┼───────────────────────┬───────────────────────┐
         ▼                       ▼                       ▼                       ▼
 ┌──────────────┐        ┌──────────────┐        ┌──────────────┐        ┌──────────────┐
 │ user-service │        │train-service │        │booking-service│       │payment-service│
 │    :8081     │        │    :8082     │        │    :8083     │        │    :8084     │
 └──────┬───────┘        └──────┬───────┘        └──────┬───────┘        └──────┬───────┘
        │                       │                       │                       │
        ▼                       ▼                       ▼                       ▼
 ┌──────────────┐        ┌──────────────┐        ┌──────────────┐        ┌──────────────┐
 │   user_db    │        │   train_db   │        │  booking_db  │        │  payment_db  │
 └──────┬───────┘        └──────┬───────┘        └──────┬───────┘        └──────┬───────┘
        │                       │                       │                       │
        └───────────────────────┴─────────┬─────────────┴───────────────────────┘
                                          │
                                          ▼ Shared Infrastructure
                                ┌──────────────────┐
                                │   Redis :6379    │
                                └──────────────────┘

                                        │ Kafka Events
                                        ▼
                               ┌──────────────────┐
                               │notification-serv.│ :8085
                               └──────────────────┘
```

- **Service Discovery**: Eureka Server (`:8761`)
- **API Gateway**: Spring Cloud Gateway (`:8080`)
- **Event Streaming**: Apache Kafka (`:9092`) & Zookeeper (`:2181`)
- **Metrics & Observability**: Prometheus (`:9090`), Grafana (`:3000`), and Zipkin (`:9411`)
- **Code Quality**: SonarQube (`:9000`)

---

## Prerequisites

| Tool | Version | Notes |
|---|---|---|
| Java JDK | 17+ | Set `JAVA_HOME` |
| Apache Maven | 3.6+ | Available in `PATH` |
| Docker Desktop | Any recent | Must be **running** before startup |

---

## Quick Start (Recommended)

### Option A — One-command launch

Open PowerShell **as Administrator** in the project root and run:

```powershell
.\start-local.ps1
```

This will:
1. Pull and start infrastructure containers (**MySQL 8**, **Redis 7**, **Kafka/Zookeeper**, **Prometheus/Grafana**, **Zipkin**, **SonarQube**).
2. Wait until MySQL and Redis are healthy.
3. Build the project with Maven (skipping tests).
4. Launch each service in its own terminal window.

### Option B — Manual step-by-step

#### 1. Start infrastructure

```powershell
docker compose up -d
```

Wait ~30 seconds for MySQL and Kafka to initialise.

#### 2. Build the shared library

```powershell
cd common-utils
mvn install -DskipTests
cd ..
```

#### 3. Build all services

```powershell
mvn install -DskipTests
```

#### 4. Start services (each in a separate terminal, in this order)

```powershell
# Terminal 1 — Service Discovery (wait ~20s before starting others)
java -jar eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar

# Terminal 2 — API Gateway
java -jar api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar

# Terminal 3 — User Service
java -jar user-service/target/user-service-0.0.1-SNAPSHOT.jar

# Terminal 4 — Train Service
java -jar train-service/target/train-service-0.0.1-SNAPSHOT.jar

# Terminal 5 — Booking Service
java -jar booking-service/target/booking-service-0.0.1-SNAPSHOT.jar

# Terminal 6 — Payment Service
java -jar payment-service/target/payment-service-0.0.1-SNAPSHOT.jar

# Terminal 7 — Notification Service
java -jar notification-service/target/notification-service-0.0.1-SNAPSHOT.jar
```

---

## Service Endpoints & Tools

| Service / Tool | Local URL | Purpose |
|---|---|---|
| **Eureka Dashboard** | http://localhost:8761 | Service registry UI |
| **API Gateway** | http://localhost:8080 | All external requests go here |
| **user-service** | http://localhost:8081 | User signup, login, profile management |
| **train-service** | http://localhost:8082 | Train schedules & search |
| **booking-service** | http://localhost:8083 | Handles ticket bookings |
| **payment-service** | http://localhost:8084 | Processes transactions and issues events |
| **notification-service**| http://localhost:8085 | Consumes Kafka events to send notifications |
| **SonarQube Dashboard**| http://localhost:9000 | Code quality and code coverage dashboard |
| **Prometheus** | http://localhost:9090 | Metrics collection scraper |
| **Grafana** | http://localhost:3000 | Metrics dashboard |
| **Zipkin** | http://localhost:9411 | Distributed tracing UI |
| **Booking Swagger UI** | http://localhost:8083/swagger-ui/index.html | OpenAPI docs |

### API Routes (via Gateway :8080)

| Path | Routed to |
|---|---|
| `POST /public/register` | user-service |
| `POST /public/login` | user-service |
| `GET  /user/**` | user-service |
| `GET  /trains/**` | train-service |
| `GET  /bookings/**` | booking-service |

---

## Database Configuration

The application uses **MySQL 8** with decoupled databases for each microservice to enforce the Database-per-Service pattern.

- **Host**: `localhost:3306`
- **Username**: `root`
- **Password**: `Boss@8055`
- **Databases**:
  - `user_db` (for user-service)
  - `train_db` (for train-service)
  - `booking_db` (for booking-service)
  - `payment_db` (for payment-service)
- **Schema management**: Hibernate `ddl-auto=update` (tables created automatically on first run via `init.sql`)

---

## Stopping Everything

```powershell
# Stop infra containers (including DBs, Redis, Kafka, and Monitoring tools)
.\start-local.ps1 -StopInfra

# Or manually
docker compose down
```

---

## Module Overview

| Module | Description |
|---|---|
| `common-utils` | Shared library: JPA entities, JWT utils, Spring Security config, Redis config |
| `eureka-server` | Netflix Eureka service discovery server |
| `api-gateway` | Spring Cloud Gateway — routes and load-balances via Eureka |
| `user-service` | Registration, login, JWT issuance, user profile |
| `train-service` | Train CRUD and availability data |
| `booking-service` | Booking creation/cancellation; calls train-service via Feign |
| `payment-service` | Payment processing, integrated with Kafka to produce payment events |
| `notification-service`| Listens to Kafka topics to send notifications to users |
