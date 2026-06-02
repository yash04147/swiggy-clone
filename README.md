# Swiggy Clone Backend

A production-style food delivery backend built with Java, Spring Boot, MongoDB, JWT authentication, and Docker.

This project simulates the workflow of a platform like Swiggy or Zomato:

```text
Customer places order
→ Restaurant owner accepts and prepares it
→ Delivery partner picks it up
→ Order gets delivered
```

---

# Features

## Authentication & Authorization

* JWT-based authentication
* Role-based access control
* Supported roles:

    * CUSTOMER
    * RESTAURANT_OWNER
    * DELIVERY_PARTNER

## Restaurant Management

* Restaurant owners can create restaurants
* Owners can view their own restaurants
* Customers can browse open restaurants
* Geo-based nearby restaurant search

## Order System

* Customers can place orders
* Customers can view their order history
* Restaurant owners can:

    * View incoming orders
    * Accept orders
    * Mark orders as PREPARING
    * Mark orders as READY_FOR_PICKUP
* Delivery partners can:

    * Create profile
    * Pick up READY_FOR_PICKUP orders
    * Mark orders as DELIVERED

## Production-Style Improvements

* DTO-based API responses
* Global exception handling
* MongoDB geospatial query support
* Docker Compose setup

---

# Tech Stack

* Java 21
* Spring Boot
* Spring Security
* JWT
* MongoDB
* Spring Data MongoDB
* Docker & Docker Compose
* Swagger / OpenAPI
* Lombok

---

# Project Structure

```text
src/main/java/com/yash/delivery
├── controller
├── dto
├── exception
├── model
├── repository
├── security
└── service
```

---

# API Flow

```text
CUSTOMER
Register/Login
→ Browse restaurants
→ Place order
→ View order history

RESTAURANT_OWNER
Register/Login
→ Create restaurant
→ View restaurant orders
→ Update order status

DELIVERY_PARTNER
Register/Login
→ Create delivery profile
→ Pick up order
→ Mark delivered
```

---

# Main APIs

## Authentication

```text
POST /api/auth/register
POST /api/auth/login
```

## Restaurants

```text
POST /api/restaurants
GET  /api/restaurants
GET  /api/restaurants/my
GET  /api/restaurants/nearby?lat=28.9845&lng=77.7064
```

## Orders

```text
POST  /api/orders
GET   /api/orders/my
GET   /api/orders/restaurant/{restaurantId}
PATCH /api/orders/{orderId}/status
PATCH /api/orders/{orderId}/pickup
PATCH /api/orders/{orderId}/deliver
```

## Delivery Partners

```text
POST /api/delivery-partners
```

---

# Order Lifecycle

```text
PLACED
→ ACCEPTED
→ PREPARING
→ READY_FOR_PICKUP
→ OUT_FOR_DELIVERY
→ DELIVERED
```

---

# Running Locally

## Option 1: Run With Docker Compose

```bash
docker compose up --build
```

Backend:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

MongoDB:

```text
mongodb://admin:password@localhost:27017/?authSource=admin
```

---

# Important MongoDB Geo Index

After first startup, create the geospatial index manually:

```javascript
db.Restaurants.createIndex({ location: "2dsphere" })
```

Without this index, the nearby restaurant API will not work.

---

# Example Nearby Search

```text
GET /api/restaurants/nearby?lat=28.9845&lng=77.7064
```

Returns restaurants within 5 km of the provided coordinates.

---

# Future Improvements

* Real-time order tracking with WebSockets
* Delivery partner auto-assignment
* Restaurant ratings and reviews
* Pagination and filtering
* Unit and integration tests
* CI/CD pipeline
* Kubernetes deployment

---

# Highlights

This project demonstrates:

* Secure JWT authentication
* Multi-role authorization
* REST API design
* MongoDB geospatial queries
* DTO pattern
* Dockerized backend
* Real-world order lifecycle management
