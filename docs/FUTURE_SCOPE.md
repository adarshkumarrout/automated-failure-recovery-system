# Future Scope

Recommended next steps for turning this reference project into a production-grade platform:

1. Add the transactional outbox pattern so database updates and Kafka event publication are atomic.
2. Add separate downstream adapters for payments, orders, refunds, inventory, and notifications.
3. Add a manual repair dashboard for DLQ records with replay, cancel, and annotate actions.
4. Add alerting rules in Prometheus/Grafana for high failure rate, DLQ growth, and low retry success rate.
5. Add multi-tenant support with tenant-level retry policies and rate limits.
6. Add jitter to exponential backoff to prevent thundering herd retries.
7. Add poison-message protection for malformed Kafka payloads.
8. Add a proper Redis lock token so only the owner can release a lock.
9. Add OpenTelemetry tracing across API, Kafka, Redis, MySQL, and downstream calls.
10. Add Testcontainers integration tests for Kafka, Redis, and MySQL.
11. Add a workflow engine such as Temporal, Camunda, or Netflix Conductor for long-running saga orchestration.
12. Add compensation actions for distributed transactions, such as refund payment or release inventory.
13. Add replay controls that support dry runs and approval gates.
14. Add per-operation retry strategies instead of one global policy.
15. Add authentication and authorization for operational endpoints.
16. Add schema evolution using Avro/Protobuf and a schema registry.
17. Add deployment manifests for Kubernetes, including probes, HPA, ConfigMaps, and Secrets.
18. Add chaos testing to validate behavior during Kafka, Redis, MySQL, and downstream outages.

