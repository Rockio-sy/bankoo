# Bank Card Management API

A Spring Boot REST API for managing bank cards, users, authentication, balances, and transfers.

## Features

- JWT-based authentication and registration
- User card listing with filtering by status
- Card details with masked or raw card number views for the owner
- Card block requests from users
- Money transfers between a user's own cards
- Balance lookup
- Admin card management: create, list, update status, delete
- Admin user management: list, search, view, create, delete
- AES-based card number encryption
- Global API error handling
- Swagger / OpenAPI documentation

## Tech Stack

- Java 21
- Spring Boot 3.5.4
- Spring Security with JWT
- Spring Data JPA
- PostgreSQL
- Liquibase
- SpringDoc OpenAPI
- Docker and Docker Compose
- JUnit and Mockito

## Quick Start

### 1. Clone the repository

```bash
git clone git@github.com:Rockio-sy/bankoo.git
cd bankoo/Bank_REST
```

### 2. Configure environment variables

Copy the example file and set your own values:

```bash
cp .env.example .env.prod
```

Required variables:

- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `JWT_SECRET`
- `ENCRYPTION_KEY`

### 3. Run with Docker

```bash
docker-compose --env-file .env.prod up --build
```

The application runs at `http://localhost:8083`.

## Authentication

Register and log in through:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`

Send the JWT in the `Authorization` header:

```text
Authorization: Bearer <your_token_here>
```

## API Overview

### User endpoints

- `GET /api/v1/cards/all`
- `GET /api/v1/cards/{cardId}`
- `GET /api/v1/cards/raw/{cardId}`
- `POST /api/v1/cards/block-request/{cardId}`
- `POST /api/v1/cards/transfers`
- `GET /api/v1/cards/{cardId}/balance`

### Admin endpoints

- `POST /api/v1/admin/cards/new`
- `PATCH /api/v1/admin/cards/{cardId}/status`
- `DELETE /api/v1/admin/cards/{cardId}/delete`
- `GET /api/v1/admin/cards/all`
- `GET /api/v1/admin/cards/all/{userId}`
- `GET /api/v1/admin/users/all`
- `GET /api/v1/admin/users/{userId}`
- `POST /api/v1/admin/users/create`
- `DELETE /api/v1/admin/users/{userId}`

## Documentation

- Swagger UI: `http://localhost:8083/swagger-ui/index.html`
- OpenAPI YAML: `docs/openapi.yaml`

## Database

Liquibase migrations live in `src/main/resources/db/changelog/` and run automatically at startup.

## Notes

- The repository seeds a development admin account on startup. Change or disable that behavior for production use.
- Card numbers are stored encrypted, and API responses use masking where appropriate.
