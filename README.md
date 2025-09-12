# Task Tracking API

A clean Spring Boot 3 REST API for managing task lists and tasks. This project showcases a layered architecture (Controller → Service → Repository), DTO mapping, validation, global error handling, OpenAPI docs, and comprehensive automated tests across unit, repository, and integration layers.

## What I Learned

- Designing a clean layered architecture in Spring Boot with clear boundaries between controllers, services, repositories, DTOs, and mappers.
- Using Java records for concise, immutable DTOs and applying Jakarta Bean Validation for robust input validation.
- Building expressive automated tests: unit tests with Mockito and AssertJ, repository tests with `@DataJpaTest` + H2, and controller integration tests with MockMvc.
- Handling errors consistently using `@RestControllerAdvice`, mapping service exceptions to appropriate HTTP status codes and JSON error bodies.
- Managing entity relationships and defaults: UUID primary keys, JPA mappings, lazy loading, cascading behavior, and sensible defaults for task status/priority.
- Generating API documentation with springdoc OpenAPI and validating endpoints interactively via Swagger UI.
- Measuring test coverage with JaCoCo and organizing the project to be CI/CD‑ready (Maven life cycle, deterministic tests, in‑memory DB for fast feedback).

## Features

- Task lists: create, read, update, delete.
- Tasks within a list: create, read, update, delete.
- Validation via Jakarta Bean Validation on DTOs.
- Consistent error responses via a global exception handler.
- OpenAPI/Swagger UI auto‑generated docs.
- Test suite: unit (Mockito, AssertJ), repository (DataJpaTest/H2), integration (MockMvc).
- Code coverage with JaCoCo.

## Tech Stack

- Java 21, Maven
- Spring Boot 3.5 (Web, Data JPA)
- PostgreSQL (runtime), H2 (tests)
- Hibernate, Jakarta Validation
- MapStruct pattern (interfaces with hand‑written implementations)
- Lombok (optional in POM; no Lombok annotations in entities)
- springdoc-openapi (Swagger UI)
- JUnit 5, Mockito, AssertJ, MockMvc
- JaCoCo

## Architecture

- Domain: `TaskList`, `Task`, enums `TaskStatus { OPEN, CLOSED }`, `TaskPriority { HIGH, MEDIUM, LOW }`.
- DTOs: `TaskListDto` and `TaskDto` (Java records) with validation annotations.
- Mappers: `TaskMapperImpl`, `TaskListMapperImpl` convert between entities and DTOs.
- Repositories: Spring Data JPA interfaces for persistence.
- Services: Business logic and validations (`TaskListServiceImpl`, `TaskServiceImpl`).
- Controllers: REST endpoints (`TaskListController`, `TasksController`).
- Error handling: `GlobalExceptionHandler` maps exceptions to JSON error responses.

## API Overview

Base URL: `http://localhost:8080`

Task Lists

- GET `/task-lists` – list all task lists.
- POST `/task-lists` – create a task list.
- GET `/task-lists/{task_list_id}` – get a task list by ID.
- PUT `/task-lists/{task_list_id}` – update a task list (body.id must match path).
- DELETE `/task-lists/{task_list_id}` – delete a task list.

Tasks (scoped by task list)

- GET `/task-lists/{task_list_id}/tasks` – list tasks in a list.
- POST `/task-lists/{task_list_id}/tasks` – create a task in a list.
- GET `/task-lists/{task_list_id}/tasks/{task_id}` – get a task by ID.
- PUT `/task-lists/{task_list_id}/tasks/{task_id}` – update a task (body.id must match path).
- DELETE `/task-lists/{task_list_id}/tasks/{task_id}` – delete a task.

Data contracts (DTOs)

- TaskListDto: `{ id: UUID, title: string, description?: string, count: number, progress?: number, tasks?: TaskDto[] }`
- TaskDto: `{ id: UUID, title: string, description?: string, dueDate?: ISO-8601 string, priority?: HIGH|MEDIUM|LOW, status?: OPEN|CLOSED }`

## Local Development

Prerequisites

- JDK 21
- Maven 3.9+
- Docker (optional, for PostgreSQL)

Start PostgreSQL (optional)

- `docker compose up -d` (exposes Postgres on `localhost:5433` per `docker-compose.yaml`).

Configure application (defaults work out of the box)

- `src/main/resources/application.properties:1` contains a default PostgreSQL URL `jdbc:postgresql://localhost:5433/postgres` with username `postgres` and password `password1234!`.
- JPA ddl-auto is set to `update` for convenience during development.

Run the application

- Unix/macOS: `./mvnw spring-boot:run`
- Windows: `mvnw.cmd spring-boot:run`
- Alternatively: `./mvnw clean package` then `java -jar target/tasks-0.0.1-SNAPSHOT.jar`

OpenAPI/Swagger

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Testing & Coverage

- Run tests: `./mvnw test`
- Test types included:
  - Unit: service logic with Mockito and AssertJ
  - Repository: Spring Data JPA using `@DataJpaTest` with H2 in-memory DB
  - Integration: controller endpoints with `@SpringBootTest` + MockMvc
- Coverage report: after `./mvnw verify`, open `target/site/jacoco/index.html`

Representative tests:

- `src/test/java/com/wongsakron/tasks/controllers/TaskListControllerITTest.java:1`
- `src/test/java/com/wongsakron/tasks/controllers/TasksControllerITTest.java:1`
- `src/test/java/com/wongsakron/tasks/services/TaskListServiceImplTest.java:1`
- `src/test/java/com/wongsakron/tasks/services/TaskServiceImplTest.java:1`
- `src/test/java/com/wongsakron/tasks/repositories/TaskListRepositoryTest.java:1`
- `src/test/java/com/wongsakron/tasks/repositories/TaskRepositoryTest.java:1`

## Example Requests

Create a task list

```
POST /task-lists
Content-Type: application/json

{ "title": "My List", "description": "Personal tasks" }
```

Create a task in a list

```
POST /task-lists/{task_list_id}/tasks
Content-Type: application/json

{
  "title": "Buy groceries",
  "description": "Milk, eggs, bread",
  "dueDate": "2025-01-01T18:00:00",
  "priority": "HIGH"
}
```

Update a task (IDs must match)

```
PUT /task-lists/{task_list_id}/tasks/{task_id}
Content-Type: application/json

{
  "id": "{task_id}",
  "title": "Buy groceries and fruits",
  "description": "Milk, eggs, bread, apples",
  "dueDate": "2025-01-02T18:00:00",
  "priority": "HIGH",
  "status": "OPEN"
}
```

## Notes & Next Steps

- The project targets local development with PostgreSQL via Docker compose. For production, externalize sensitive configs and use proper Flyway/Liquibase migrations.
- Consider adding authentication/authorization, pagination/filtering for task lists, and tagging or assignment features.
- A CI pipeline (e.g., GitHub Actions) can easily run `mvn -B verify` and publish the JaCoCo report artifact.
