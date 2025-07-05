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
