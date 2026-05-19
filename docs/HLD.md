# High Level Design

## Goal

Build a distributed microservices-based failure recovery system that detects failed operations, retries recoverable failures intelligently, prevents duplicate execution, and moves unrecoverable work to a dead letter queue for investigation.

## Key Capabilities

- Detect failed API/downstream operations
- Classify failures as temporary or permanent
- Retry temporary failures using exponential backoff
- Prevent duplicate processing through idempotency keys
- Prevent concurrent retry execution through Redis distributed locks
- Publish recovery lifecycle events through Kafka
- Persist all request and retry state in MySQL
- Expose health and recovery metrics for operations teams
- Stop cascades through a circuit breaker

## Context Diagram

```mermaid
flowchart LR
    Client["Client / Upstream Service"] --> API["Recovery API Service"]
    Dashboard["React Dashboard"] --> API
    API --> DB["MySQL\nRequest + Retry State"]
    API --> Kafka["Kafka\nRecovery Events"]
    Worker["Retry Worker Service"] --> DB
    Worker --> Kafka
    Kafka --> Worker
    Worker --> Redis["Redis\nDistributed Locks"]
    Worker --> Payment["Payment Service"]
    Kafka --> Alert["DLQ Alert Service"]
    API --> Metrics["Actuator / Prometheus Metrics"]
```

## Component Architecture

```mermaid
flowchart TB
    subgraph ApiService["recovery-api-service"]
        Controller["RecoveryApiController"]
        Service["RecoveryCommandService"]
    end

    subgraph WorkerService["retry-worker-service"]
        Backoff["RetryBackoffPolicy"]
        FailureListener["FailureEventListener"]
        RetryListener["RetryEventListener"]
        Scheduler["RetryScheduler"]
        Lock["DistributedLockService"]
        PaymentClient["PaymentClient\nCircuit Breaker"]
        Metrics["RecoveryMetrics"]
    end

    subgraph PaymentService["payment-service"]
        PaymentController["PaymentController"]
    end

    subgraph DlqService["dlq-alert-service"]
        DlqListener["DlqAlertListener"]
    end

    subgraph Frontend["dashboard-ui"]
        Dashboard["Recovery Dashboard"]
    end

    Dashboard --> Controller
    Controller --> Service
    FailureListener --> Service
    Scheduler --> RetryListener
    RetryListener --> Lock
    RetryListener --> PaymentClient
    PaymentClient --> PaymentController
    RetryListener --> Metrics
```

## Event Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant API as Recovery API Service
    participant DB as MySQL
    participant K as Kafka
    participant W as Retry Worker Service
    participant R as Redis
    participant D as Payment Service
    participant A as DLQ Alert Service

    C->>API: POST /api/recovery/execute with idempotencyKey
    API->>DB: Create request if key is new
    API->>K: Publish api-failed-events
    K->>W: Failure listener schedules nextRetryAt
    W->>DB: Poll due RETRY_PENDING records
    W->>K: Publish retry-scheduled-events
    K->>W: Consume due retry
    W->>R: Acquire lock by idempotencyKey
    W->>D: Retry operation
    D-->>W: Success
    W->>DB: Mark SUCCEEDED
    W->>K: Publish retry-succeeded-events
    W->>R: Release lock
    K->>A: Consume dead-letter-events when retries are exhausted
```

## Data Ownership

MySQL is the source of truth for:

- Original request payload
- Idempotency key
- Current recovery status
- Attempt count
- Next retry time
- Last error
- Final outcome

Kafka is used for asynchronous coordination and observability events. Redis is used only for short-lived distributed locks.

## Failure Classification

| Type | Meaning | Action |
| --- | --- | --- |
| `TEMPORARY` | Timeout, network failure, transient dependency issue | Retry with exponential backoff |
| `PERMANENT` | Validation failure, business rule rejection, unrecoverable downstream response | Mark permanent failure and publish DLQ event |
| `NONE` | Simulated successful path | Mark succeeded |

## Retry Strategy

Formula:

```text
delay = min(initialDelay * 2^(attemptCount - 1), maxDelay)
```

Default values:

- Initial delay: 5 seconds
- Max delay: 300 seconds
- Max attempts: 5

## Consistency Model

The system uses at-least-once event delivery and makes processing idempotent:

- The unique `idempotencyKey` prevents creating or executing duplicate requests.
- MySQL stores current status and attempt count.
- Redis lock prevents multiple service instances from retrying the same key at the same time.
- Kafka consumers are allowed to receive duplicate events; DB status checks make duplicates harmless.

## Monitoring

Actuator exposes:

- `/actuator/health`
- `/actuator/metrics`
- `/actuator/prometheus`
- `/actuator/circuitbreakers`

Custom metrics:

- `recovery_failures_total`
- `recovery_retry_success_total`
- `recovery_retry_failure_total`
- `recovery_dead_letter_total`
