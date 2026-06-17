# Architecture

Dev Platform is a small internal developer platform that gives service teams a consistent deployment workflow while keeping Kubernetes details behind a stable API and UI.

## Goals

- Standardize service and environment registration.
- Track what image is deployed where.
- Preserve deployment status and history for incident response.
- Emit deployment events for downstream automation.
- Keep Kubernetes, Helm, and cloud infrastructure definitions versioned.

## Non-Goals

- Replacing a full CI/CD system.
- Implementing a production approval workflow.
- Managing every Kubernetes object type directly.
- Replacing GitOps controllers such as Argo CD or Flux.

## Components

```text
Developer / UI / CLI
        |
        v
Spring Boot API
        |
        +--> PostgreSQL: services, environments, deployments, history
        |
        +--> RabbitMQ: deployment-created and deployment-status events
        |
        +--> Kubernetes desired state layer
                |
                +--> Helm chart
                +--> Static manifests
                +--> GitOps examples
```

## Deployment Flow

1. A developer registers a service and environment.
2. A deployment request is submitted through the UI, API, or CLI.
3. The API validates platform policy.
4. The deployment is stored as `PENDING`.
5. A RabbitMQ event is published after the database transaction commits.
6. A deployment worker or Kubernetes integration can consume the event and apply the desired state.
7. Status updates are recorded as history entries.
8. A successful deployment becomes the current deployment for that service and environment.

## Rollback Flow

`POST /v1/deployments/{id}/rollback` finds the previous successful deployment for the same service and environment, then creates a new `PENDING` deployment using that earlier image tag. The rollback is intentionally modeled as a new deployment instead of mutating the previous record, preserving audit history.

## Data Model

- `Service`: deployable application metadata, including owner and image registry.
- `Environment`: target runtime metadata, including namespace and cluster context.
- `Deployment`: requested image, status, current flag, idempotency key, and timestamps.
- `History`: status transitions for deployment auditability.

## Policy Guardrails

Production deployments enforce a small set of platform rules:

- image tag must be immutable, not `latest`
- service owner must be defined
- deployer identity must be supplied
- production environment must define a namespace

These checks live server-side in `DeploymentPolicy` so every client path gets the same behavior.

## Kubernetes Boundary

The repo includes three levels of Kubernetes ownership:

- `infra/k8s`: readable static manifests for review and learning.
- `infra/helm/dev-platform`: parameterized chart for repeatable installs.
- `infra/terraform`: cloud infrastructure and Helm release wiring.

The Kubernetes manifests include probes, resource requests and limits, service accounts, RBAC, pod disruption budgets, autoscaling, network policies, and Prometheus discovery annotations.

## Observability

The API exposes Spring Actuator health and Prometheus metrics. Important signals:

- API latency and error rate
- deployment status transitions
- rollback creation count
- RabbitMQ queue depth
- pod restart count
- database connection pool health

Example alert rules and a dashboard seed live in `infra/observability`.

## Tradeoffs

The project currently keeps the Kubernetes apply layer lightweight so the platform workflow is easy to inspect. In a larger implementation, the deployment event consumer would either call the Kubernetes API directly with bounded RBAC or write desired state into a GitOps repository and let Argo CD or Flux reconcile it.
