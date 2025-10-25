# 🚗 Car Sharing App

**Car Sharing App** is a full-stack web application built with Spring Boot that enables users to book, manage, and pay for car rentals. It supports role-based access for customers and managers, integrates with Stripe for payments, and uses Telegram for notifications.

---

## 🧰 Tech Stack

- **Java 17 + Spring Boot** — backend framework  
- **Spring Security** — authentication & authorization  
- **Spring Data JPA + Hibernate** — ORM and database access  
- **Liquibase** — database migrations  
- **MySQL** — relational database  
- **Stripe API** — payment processing  
- **Telegram Bot API** — notifications  
- **Swagger/OpenAPI** — interactive API documentation  

---

## 👥 Roles

- **Customer** — can browse cars, make bookings, and manage their rentals  
- **Manager** — can manage car listings, approve bookings, and view analytics  

---

## 📦 Features

- Car listing and filtering  
- Booking system with availability checks  
- Stripe-based payment flow  
- Telegram notifications for booking updates  
- Role-based access control  
- API documentation via Swagger UI  

---

## 🚀 Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/JlusS/car-sharing-app.git
   ```

2. Configure your `.env` or `application.yml` with:
   - MySQL credentials  
   - Stripe API keys  
   - Telegram bot token  

3. Run database migrations:
   ```bash
   ./mvnw liquibase:update
   ```

4. Start the application:
   ```bash
   ./mvnw spring-boot:run
   ```

5. Access Swagger UI at:
   ```
   http://localhost:8080/swagger-ui/index.html
   ```

---

## 📄 API Documentation

Interactive API docs are available via Swagger. You can also import the Postman collection from [`docs/postman_collection.json`](docs/postman_collection.json).

📥 Installation
Clone the repository and install dependencies:

# Clone the repo
git clone https://github.com/JlusS/car-sharing-app

# Navigate into the project
cd carrental

# Build with Docker:
docker build -t carrental .

# Or with Maven:
mvn install
▶️ Usage
Run with Docker:
docker run -it carrental

Run with Maven:
mvn spring-boot:run
Once running, access: Application: http://localhost:8080

Swagger UI: http://localhost:8080/swagger-ui.html

API Documentation: http://localhost:8080/v3/api-docs

🧪 Testing
Run tests with Maven:
mvn test

Run tests in Docker:
docker run -it carrental mvn test

---

## 🧪 Testing

- Unit and integration tests are written using JUnit and Testcontainers.  
- To run tests:
  ```bash
  ./mvnw test
  ```

---

## 🤝 Contributing

Pull requests are welcome! Please make sure your code is clean, tested, and documented.
