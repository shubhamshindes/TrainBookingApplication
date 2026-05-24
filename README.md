# 🚆 Train Booking Application

A secure and scalable Train Booking System built using **Spring Boot**, implementing **JWT-based Authentication and Authorization**. It allows users to register, log in, search for trains, and book tickets securely.

---

## 🧰 Tech Stack

- **Java 17**
- **Spring Boot**
- **Spring Security + JWT**
- **Spring Data JPA**
- **MySQL**
- **Lombok**
- **Maven**
- **Postman** (for API testing)

---

## 🔐 Features

### 👥 User Module
- Register new user
- User login with JWT token generation
- Role-based access (e.g., USER, ADMIN)
- Token-protected endpoints

### 🚄 Train Module
- Add new trains (Admin)
- View all available trains
- Search trains by name 

### 🎟️ Booking Module
- Book a ticket for a train
- View user-specific bookings
- Cancel a booking
- Check Available Seats

---

## 📦 API Endpoints Overview

### 🔐 Auth (JWT Secured)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST   | `/api/auth/register` | Register a new user |
| POST   | `/api/auth/login`    | Authenticate and get JWT token |

---

### 👤 User APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | `/api/users/me`        | Get current user info |
| GET    | `/api/users/all`       | View all users (Admin only) |

---

### 🚄 Train APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | `/api/trains`         | Get all trains |
| POST   | `/api/trains`         | Add train (Admin only) |
| GET    | `/api/trains/{id}`    | Get train by ID |
| DELETE | `/api/trains/{id}`    | Delete train (Admin only) |

---

### 🎟️ Booking APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST   | `/api/bookings`       | Book a train |
| GET    | `/api/bookings`       | Get user's bookings |
| DELETE | `/api/bookings/{id}`  | Cancel booking |

---

## 🔑 How JWT Works Here

1. User logs in and receives a **JWT token**
2. All protected endpoints must include the token in `Authorization` header:
   - Format: `Authorization: Bearer {token}`

---

## Running locally with Docker (app + Redis)

This repository includes a Dockerfile and `docker-compose.yml` to run the application together with Redis for local development.

How it works:
- `docker-compose.yml` starts a Redis container and the application container.
- For convenience the compose file overrides DB settings to an in-memory H2 database so you don't need MySQL locally for smoke tests.

Commands

1. Build and start the stack:

    ```powershell
    cd "C:\D\Program and DOC\NewlyPracticeAdv\TrainBookingApplication"
    docker compose up --build -d
    ```

2. Check logs (app):

    ```powershell
    docker compose logs -f app
    ```

3. Verify Redis keys (optional):

    ```powershell
    docker exec -it train-redis redis-cli keys "*"
    ```

4. Stop and remove containers:

    ```powershell
    docker compose down
    ```

Notes:
- To run the app against your MySQL instance instead of H2, remove the H2 overrides in `docker-compose.yml` and set the `spring.datasource.*` environment variables appropriately (or run the app without docker-compose and use your local configuration in `application.properties`).
- To use Upstash, set `spring.redis.url` to your Upstash `rediss://...` URL (do not commit secrets to source control).
