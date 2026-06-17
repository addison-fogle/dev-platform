# Observability

The backend exposes Prometheus metrics at `/actuator/prometheus` and Kubernetes health probes at `/actuator/health/liveness` and `/actuator/health/readiness`.

## Signals

- API request rate, latency, and 5xx count
- deployment status transitions
- rollback creation count
- RabbitMQ queue depth
- pod restarts and readiness
- database connection pool metrics

## Files

- `prometheus-alerts.yaml`: example Prometheus alert rules
- `grafana-dashboard.json`: starter dashboard for API and deployment signals
