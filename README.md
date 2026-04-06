# Task Manager API

A Spring Boot REST API for managing tasks with status tracking.

## Tech Stack
- Java 25
- Spring Boot 4.0.5
- Spring Data JPA
- H2 (in-memory database)
- Maven

## Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /task | Get all tasks |
| GET | /task/{id} | Get task by id |
| POST | /task | Create new task |
| DELETE | /task/{id} | Delete task |
| PUT | /task/{id}/status | Update task status |

## Status Values
Only accepts: `TODO`, `IN_PROGRESS`, `DONE`

## Run Locally
```bash
./mvnw spring-boot:run
```
