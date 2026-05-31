# IRCTC Microservices — Local Development Guide

A Spring Boot microservices application for train booking (IRCTC-style), built with Java 17, Spring Cloud, MySQL, and Redis.

---

## Architecture

```
                        ┌──────────────────┐
    Client              │   API Gateway    │  :8080
    ───────────────────▶│  (Spring Cloud)  │
                        └────────┬─────────┘
                                 │ routes via Eureka
              ┌──────────────────┼──────────────────┐
              ▼                  ▼                   ▼
      ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐
      │ user-service │  │train-service │  │ booking-service  │
      │    :8081     │  │    :8082     │  │     :8083        │
      └──────┬───────┘  └──────┬───────┘  └────────┬─────────┘
             │                 │                    │
             └─────────────────┼────────────────────┘
                               ▼
                   ┌───────────────────────┐
                   │  MySQL :3306  +        │
                   │  Redis :6379           │
                   └───────────────────────┘

      Eureka Server (Service Discovery) :8761
```

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
1. Pull and start **MySQL 8** + **Redis 7** Docker containers
2. Wait until both are healthy
3. Build the project with Maven (skipping tests)
4. Launch each service in its own terminal window

### Option B — Manual step-by-step

#### 1. Start infrastructure

```powershell
docker compose -f docker-compose.infra.yml up -d
```

Wait ~30 seconds for MySQL to initialise.

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
```

---

## Service Endpoints

| Service | Local URL | Purpose |
|---|---|---|
| Eureka Dashboard | http://localhost:8761 | Service registry UI |
| API Gateway | http://localhost:8080 | All external requests go here |
| user-service | http://localhost:8081 | Direct access (dev only) |
| train-service | http://localhost:8082 | Direct access (dev only) |
| booking-service | http://localhost:8083 | Direct access (dev only) |
| Booking Swagger UI | http://localhost:8083/swagger-ui/index.html | OpenAPI docs |

### API Routes (via Gateway :8080)

| Path | Routed to |
|---|---|
| `POST /public/register` | user-service |
| `POST /public/login` | user-service |
| `GET  /user/**` | user-service |
| `GET  /trains/**` | train-service |
| `GET  /bookings/**` | booking-service |

---

## Database

- **Host**: `localhost:3306`
- **Database**: `train_booking_db`
- **Username**: `root`
- **Password**: `Boss@8055`
- **Schema management**: Hibernate `ddl-auto=update` (tables created automatically on first run)

---

## Stopping Everything

```powershell
# Stop infra containers (MySQL + Redis)
.\start-local.ps1 -StopInfra

# Or manually
docker compose -f docker-compose.infra.yml down
```

---

## Useful Script Flags

```powershell
.\start-local.ps1                  # Full startup (build + infra + services)
.\start-local.ps1 -SkipBuild      # Skip Maven build (use existing JARs)
.\start-local.ps1 -InfraOnly      # Start only MySQL + Redis (no Spring services)
.\start-local.ps1 -StopInfra      # Stop and remove infra containers
```

---

## Running with Full Docker (CI / Production-like)

To run everything in Docker containers (including the Spring Boot services):

```powershell
docker compose up --build
```

> ⚠️ This builds Docker images for every service — takes longer but mirrors production.

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
