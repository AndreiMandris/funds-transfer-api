# Funds Transfer API

A RESTful API for funds transfers with currency exchange, built with Spring Boot 3 and PostgreSQL.

## Quick Start

```bash
# Start with Docker Compose
docker-compose up -d

# API is available at
http://localhost:8080/api/v1

# Swagger UI
http://localhost:8080/api/v1/swagger-ui.html
```

## API Endpoints

### Accounts
- `POST /accounts` - Create account
- `GET /accounts/{id}` - Get account
- `GET /accounts` - List all accounts
- `DELETE /accounts/{id}` - Delete account

### Transfers
- `POST /transfers` - Create transfer
- `GET /transfers/{id}` - Get transfer
- `GET /transfers` - List all transfers

## Example Usage

```bash
# Create accounts
curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{"ownerId": 1, "currency": "USD", "initialBalance": 1000.00}'

# Transfer funds
curl -X POST http://localhost:8080/api/v1/transfers \
  -H "Content-Type: application/json" \
  -d '{"fromAccountId": "account1", "toAccountId": "account2", "amount": 100.00}'
```

## Tech Stack

- Java 21
- Spring Boot 3.2.0
- PostgreSQL 15
- Docker & Docker Compose
- Swagger UI

## Development

```bash
# Run tests
mvn test

# Build
mvn clean package

# Run locally (requires PostgreSQL)
mvn spring-boot:run
```
