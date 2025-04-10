# CloudService

CloudService is a cloud file storage service built using Spring Boot 3 and Java 21. The service leverages PostgreSQL for storing user and file metadata, Liquibase for database migrations, and MinIO as an S3-compatible object store to manage file content. CloudService implements token-based authentication using Spring Security.

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Testing](#testing)
- [License](#license)

## Features

- **User Management:**  
  User registration and authentication using a custom `UserDetailsService` and token-based authentication.

- **File Management:**  
  Upload, update, delete, and retrieve file metadata stored in PostgreSQL.  
  The actual file content is stored in MinIO (S3-compatible storage).

- **Error Handling:**  
  Centralized error handling through a global exception handler that wraps errors in a custom `ErrorResponse` DTO.

- **Database Migrations:**  
  Schema and data changes are managed using Liquibase.

## Architecture

- **Spring Boot:**  
  Application built on Spring Boot 3 and Java 21 with a layered architecture (Controller, Service, Repository).

- **PostgreSQL & Liquibase:**  
  PostgreSQL stores user, token, and file metadata. Liquibase manages the schema via changelogs located under `src/main/resources/db/changelog`.

- **MinIO:**  
  Used as the object store for file content. A MinIO bucket is automatically initialized at application startup.

- **Spring Security:**  
  Token-based authentication with a custom authentication filter (`AuthTokenFilter`) and `UserDetailsService`.

## Prerequisites

- **Java 21**
- **Gradle** (or use the included Gradle Wrapper `gradlew`/`gradlew.bat`)
- **Docker:** For running PostgreSQL, MinIO, and other services locally.
- **PostgreSQL:** Ensure you have a PostgreSQL server running (or use the provided Docker Compose configuration).
- **MinIO:** Used as the object storage; see Docker Compose section below.

## Configuration

Application configuration:

📄 **[application.properties](src/main/resources/application.properties)**

## Running the Application

Use docker-compose for initializing PostgreSQL and MinIO. Afterward, run Spring Boot Application.

📄 **[docker-compose.yaml](docker-compose.yaml)**

## API Endpoints

The API is documented in the OpenAPI specification file:

📄 **[CloudServiceSpecification.yaml](src/main/resources/CloudServiceSpecification.yaml)**

You can import this file into tools like [Swagger UI](https://swagger.io/tools/swagger-ui/), [Postman](https://www.postman.com/), or [Insomnia](https://insomnia.rest/) to explore and test the endpoints interactively.

## Testing
TODO

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.