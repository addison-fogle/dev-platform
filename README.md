# Development Platform

A lightweight internal developer platform for standardizing how services are deployed, tracked, and operated on Kubernetes.

This project models a common platform engineering problem: teams need a paved path for deployments, a reliable source of truth for what is running, and operational guardrails that reduce production risk without forcing every service team to become Kubernetes experts.

---

## Why this exists

Kubernetes is powerful but low-level. In many teams:

- deployments are inconsistent
- configurations vary between developers
- there is no clear record of what is running where
- teams do not know exactly what version is on production
- too much outage time is spent discovering deployment versions
- one configuration mistake can bring down an entire service

This project introduces a service layer that:

- standardizes deployments
- simplifies developer workflows
- tracks deployment state centrally
- enforces production deployment guardrails
- exposes platform telemetry for operators

---

## What it does

- Register services and environments
- Deploy containers to Kubernetes
- Store deployment history and status
- Show current deployments by environment
- Roll back to a previous successful deployment
- Publish deployment lifecycle events to RabbitMQ
- Expose Prometheus metrics through Spring Actuator
- Package the platform with Docker, Kubernetes manifests, Helm, and Terraform

---

## Core components

- Registry: source of truth for services and environments
- Deployment service: handles deploy, status, history, and rollback requests
- Policy layer: validates production deployment guardrails
- Event bus: publishes deployment lifecycle events
- Database: tracks services, environments, deployments, and history
- Kubernetes layer: static manifests, Helm chart, and Terraform wiring
- UI: service, environment, deployment, history, and rollback workflows
- CLI: command-line access to common platform actions

---

## How it works

1. Register a service.
2. Define environment configs such as dev, staging, and production.
3. Trigger a deployment through the API, UI, or CLI.
4. The platform validates deployment policy.
5. The platform records the deployment as pending.
6. The platform publishes an event for downstream automation.
7. Kubernetes desired state is applied directly or through a GitOps workflow.
8. Deployment status and history are tracked.
9. Current production versions are visible from one place.

---

## What this demonstrates

- Spring Boot API design with validation, exception handling, idempotency, and transactional event publication
- React UI for service, environment, and deployment workflows
- Rollback workflow modeled as a new auditable deployment
- Production deployment guardrails for immutable tags, ownership, deployer identity, and namespaces
- PostgreSQL persistence with JPA repositories
- RabbitMQ event publishing
- Prometheus metrics and Kubernetes health probes
- Docker Compose for dependent services
- Kubernetes manifests with probes, resource limits, service accounts, RBAC, PDBs, HPAs, and NetworkPolicies
- Helm chart packaging
- Terraform infrastructure wiring
- GitHub Actions CI for backend, frontend, Docker, Helm, and Terraform checks
- GitOps-oriented desired-state examples

---

## Architecture

See [docs/architecture.md](docs/architecture.md) for the component diagram, deployment flow, rollback flow, data model, Kubernetes boundary, observability model, and tradeoffs.

Related docs:

- [Deployment policies](docs/policies.md)
- [GitOps integration](docs/gitops.md)
- [Observability](infra/observability/README.md)

---

## Example API

### Deploy

```http
POST /v1/deployments
Content-Type: application/json
Idempotency-Key: deploy-payments-v1
```

```json
{
  "serviceName": "payments-api",
  "environment": "dev",
  "imageTag": "payments-api:v1",
  "deployedBy": "alice"
}
```

### Roll Back

```http
POST /v1/deployments/42/rollback
Content-Type: application/json
```

```json
{
  "deployedBy": "alice"
}
```

### Current Deployments

```http
GET /v1/deployments/current?environment=production
```

---

## CLI

The repo includes a small shell CLI for common platform workflows:

```bash
bin/dev-platform services list
bin/dev-platform environments list
bin/dev-platform deploy payments-api --env dev --image payments-api:v1 --by alice
bin/dev-platform deployments current --env production
bin/dev-platform rollback 42 --by alice
```

Set `DEV_PLATFORM_URL` to point the CLI at a non-default API host.

---

## Production guardrails

Production deployments are identified by environment name `prod` or `production`.

For production deployments, the platform requires:

- immutable image tags, not `latest`
- service owner
- deployer identity
- environment namespace

These are intentionally small, readable rules that show where platform policy belongs.

---

## Platform artifacts

- `.github/workflows/ci.yml`: backend, frontend, Docker, Helm, and Terraform validation
- `infra/k8s`: static Kubernetes manifests
- `infra/helm/dev-platform`: Helm chart
- `infra/terraform`: AWS/EKS infrastructure wiring
- `infra/observability`: Prometheus alerts and Grafana dashboard seed
- `infra/gitops`: desired-state example for GitOps workflows

---

## Limitations

- No full CI/CD pipeline orchestrator
- Minimal authentication
- Kubernetes apply worker is intentionally lightweight
- No production approval workflow yet

---

## Future Improvements

- GitHub/GitLab pull-request based deployment approvals
- Argo CD or Flux controller integration
- Image signing and provenance checks
- Service catalog ownership metadata
- Deployment notifications
- Multi-cluster environment routing
- SLO and incident integration

## License

MIT License
