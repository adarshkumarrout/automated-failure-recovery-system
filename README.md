# Automated Failure Recovery System for Microservices

A distributed Spring Boot microservices reference implementation for reliable failure recovery.

It demonstrates:

- Event-driven failure handling with Apache Kafka
- Intelligent retries with exponential backoff
- Dead letter queue handling
- Idempotency to prevent duplicate execution
- Redis distributed locks for retry coordination
- Temporary vs permanent failure classification
- Circuit breaker protection
- Monitoring with Spring Actuator and Prometheus metrics
- MySQL persistence for request/retry state

## Architecture Docs

- [High Level Design](docs/HLD.md)
- [Low Level Design](docs/LLD.md)
- [Future Scope](docs/FUTURE_SCOPE.md)

## Prerequisites

- Java 17+
- Maven 3.9+
- Docker Desktop

## Services

| Service | Port | Responsibility |
| --- | --- | --- |
| `recovery-api-service` | 8080 | Accepts requests, enforces idempotency, publishes failure events |
| `retry-worker-service` | 8081 | Schedules retries, consumes retry events, uses Redis locks, calls payment service |
| `dlq-alert-service` | 8082 | Consumes DLQ events and emits operational alerts/logs |
| `payment-service` | 8083 | Simulated downstream payment microservice |
| `dashboard-ui` | 3000 | React dashboard for request status, flow, and demo scenarios |
| Kafka UI | 8090 | Inspect Kafka topics/messages |

## Run Locally With Docker

Build and start all services:

```bash
docker compose up --build
```

Or run in the background:

```bash
docker compose up --build -d
```

## Run Locally From Maven

Start only infrastructure:

```bash
docker compose up -d mysql redis kafka kafka-ui
```

Then run services in separate terminals:

```bash
mvn -pl recovery-api-service spring-boot:run
mvn -pl retry-worker-service spring-boot:run
mvn -pl dlq-alert-service spring-boot:run
mvn -pl payment-service spring-boot:run
```

Recovery API:

```text
http://localhost:8080
```

Dashboard:

```text
http://localhost:3000
```

Kafka UI:

```text
http://localhost:8090
```

Actuator health:

```text
http://localhost:8080/actuator/health
```

Prometheus metrics:

```text
http://localhost:8080/actuator/prometheus
```

## Try The API

Submit a successful operation:

```bash
curl -X POST http://localhost:8080/api/recovery/execute ^
  -H "Content-Type: application/json" ^
  -d "{\"idempotencyKey\":\"order-1001\",\"operationType\":\"PAYMENT\",\"payload\":\"{\\\"amount\\\":500}\",\"failureMode\":\"NONE\"}"
```

Submit a temporary failure that succeeds after retries:

```bash
curl -X POST http://localhost:8080/api/recovery/execute ^
  -H "Content-Type: application/json" ^
  -d "{\"idempotencyKey\":\"order-1002\",\"operationType\":\"PAYMENT\",\"payload\":\"{\\\"amount\\\":900}\",\"failureMode\":\"TEMPORARY\",\"failuresBeforeSuccess\":2}"
```

Submit a permanent failure that goes to DLQ:

```bash
curl -X POST http://localhost:8080/api/recovery/execute ^
  -H "Content-Type: application/json" ^
  -d "{\"idempotencyKey\":\"order-1003\",\"operationType\":\"PAYMENT\",\"payload\":\"{\\\"amount\\\":1200}\",\"failureMode\":\"PERMANENT\"}"
```

Check request status:

```bash
curl http://localhost:8080/api/recovery/order-1002
```

List recent records:

```bash
curl http://localhost:8080/api/recovery
```

## Topic Design

The application creates and uses these Kafka topics:

- `api-failed-events`
- `retry-scheduled-events`
- `retry-succeeded-events`
- `retry-failed-events`
- `dead-letter-events`

## Important Local Ports

| Component | Port |
| --- | --- |
| Recovery API Service | 8080 |
| Retry Worker Service | 8081 |
| DLQ Alert Service | 8082 |
| Payment Service | 8083 |
| React Dashboard | 3000 |
| MySQL | 3306 |
| Redis | 6379 |
| Kafka | 9092 |
| Kafka UI | 8090 |

## Dashboard Features

The React dashboard shows:

- Total recovery requests
- Successful recoveries
- Pending retries
- Requests currently retrying
- Permanently closed requests, including DLQ and permanent failures
- Retry success rate
- Live request table with attempt count, next retry time, and last error
- A form to submit success, temporary failure, and permanent failure scenarios
- Links to API health, Prometheus metrics, and Kafka UI
