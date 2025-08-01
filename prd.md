# Product Requirements Document (PRD)

## Project Title
Spring Boot Database Locking Strategy Showcase

## Objective
To create a Spring Boot project that demonstrates various database locking strategies, providing clear examples and explanations for each. The project will serve as a reference for developers to understand and implement appropriate locking mechanisms based on different scenarios.

## Target Audience
- Java/Spring Boot developers
- Backend engineers
- Students and professionals interested in database concurrency and locking

## Key Features
1. **Multiple Locking Strategies**
   - Pessimistic Locking
   - Optimistic Locking
   - Row-level Locking
   - Table-level Locking
   - Application-level Locking (e.g., using Redis)
2. **Scenario-based Demonstrations**
   - Each strategy will be demonstrated with a real-world scenario (e.g., ticket booking, inventory management).
3. **REST API Endpoints**
   - Expose endpoints to trigger and observe locking behavior.
4. **Documentation & Diagrams**
   - Clear documentation and sequence diagrams for each strategy.
5. **Test Cases**
   - Automated tests to showcase race conditions and locking effectiveness.

## Out of Scope
- Production-grade security
- UI frontend (initially)

## Success Criteria
- Each locking strategy is clearly demonstrated and documented.
- Code is clean, modular, and easy to understand.
- Project is easily runnable via Docker or local setup.

## Milestones
1. Project setup and initial documentation
2. Implement Pessimistic Locking scenario
3. Implement Optimistic Locking scenario
4. Add other strategies and scenarios
5. Documentation and test coverage

## Development Approach: Test-Driven Development (TDD)

This project will be developed using a Test-Driven Development (TDD) approach to ensure correctness, clarity, and maintainability. The TDD workflow will be applied to each locking strategy and scenario as follows:

### TDD Workflow
1. **Write a Failing Test**: For each feature or scenario (e.g., Pessimistic Locking in ticket booking), start by writing a test that describes the expected behavior, including race condition handling and correct locking.
2. **Implement Minimal Code**: Write the minimal amount of code necessary to make the test pass.
3. **Refactor**: Clean up and refactor the code for readability and maintainability, ensuring all tests still pass.
4. **Repeat**: Continue this cycle for each new feature, scenario, or locking strategy.

### Test Organization
- All tests will be placed under `/src/test/java` following the package structure.
- Scenario-based test classes will be created, e.g., `PessimisticLockingTest`, `OptimisticLockingTest`, etc.
- Tests will cover both positive and negative cases, including concurrent access and race conditions.

### Tools & Frameworks
- **JUnit 5** for unit and integration testing
- **Mockito** for mocking dependencies where needed
- **Spring Boot Test** for end-to-end and API tests

### Example TDD Steps for a Locking Strategy
1. Write a test simulating concurrent access to a shared resource (e.g., booking the same ticket).
2. Run the test and observe failure (expected at first).
3. Implement the locking mechanism.
4. Run the test and ensure it passes.
5. Refactor and document the solution.

This approach will be followed for each milestone and locking strategy to ensure robust, well-documented, and reliable code.

## Repository Structure (Proposed)
- `/src/main/java` - Source code
- `/src/test/java` - Test cases
- `/docs` - Documentation and diagrams
- `/README.md` - Project overview
- `/prd.md` - Product Requirements Document

## Current Implementation Status

### ✅ Completed Features
- **Pessimistic Locking**: Implemented with `Ticket` entity using `@Lock(LockModeType.PESSIMISTIC_WRITE)` annotation
- **Optimistic Locking**: Implemented with `InventoryItem` entity using `@Version` annotation
- **Test Coverage**: Comprehensive concurrent testing for both locking strategies
- **Entity Models**: Clean JPA entities with proper annotations
- **Repository Layer**: Spring Data JPA repositories with custom locking methods

## Next Steps (Minimal Enhancement)

### 1. **Add REST API Endpoints**
- Create simple controllers to demonstrate locking via HTTP:
  - `POST /tickets/{id}/book` - Demonstrate pessimistic locking
  - `PUT /inventory/{id}/update-quantity` - Demonstrate optimistic locking
- Basic error handling for lock failures

### 2. **Improve Documentation**
- Add simple README with usage examples
- Document when to use each locking strategy
- Include basic sequence diagrams or flow charts

### 3. **Optional: Additional Database Support**
- Add MySQL configuration example alongside H2
- Show how locking behaves with real databases

## Simple Success Criteria
- REST endpoints work and demonstrate locking behavior
- Clear documentation with examples
- Easy to run and understand for educational purposes

---

This document will be updated as the project evolves.
