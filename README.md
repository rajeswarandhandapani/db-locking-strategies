# Spring Boot Database Locking Strategy Showcase

This project demonstrates various database locking strategies using Spring Boot and Java. It is intended as a reference for developers to understand and implement appropriate locking mechanisms based on different scenarios.

## Features
- Pessimistic Locking
- Optimistic Locking
- Row-level Locking
- Table-level Locking
- Application-level Locking (e.g., using Redis)
- Scenario-based demonstrations (e.g., ticket booking, inventory management)
- REST API endpoints to trigger and observe locking behavior
- Automated tests to showcase race conditions and locking effectiveness

## Getting Started
1. Clone the repository
2. Build the project with Maven
3. Run the application

## Requirements
- Java 17+
- Maven 3.6+

## Running the Application
```
mvn spring-boot:run
```

## Documentation
See the `/docs` folder and the Product Requirements Document (`prd.md`) for more details.

---

This project is for educational and demonstration purposes only.
