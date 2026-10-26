# Dev Platform

A lightweight internal developer platform that gives service teams a paved path for deploying to Kubernetes: one API, UI, and CLI for registering services, requesting deployments, enforcing production guardrails, and seeing exactly what version is running where.

**Stack:** Java 21 · Spring Boot 4 · Spring Security (OAuth2/OIDC) · PostgreSQL · RabbitMQ · React + TypeScript · Docker · Kubernetes · Helm · Terraform (AWS EKS) · Prometheus/Grafana · GitHub Actions · Semgrep · Grype

---

## Highlights

### Backend
- REST API under `/v1` with an [OpenAPI 3.1 spec](dev-platform-api.yaml), bean validation, and centralized error handling
- **Idempotent writes**: `Idempotency-Key` header backed by a unique constraint, safe under concurrent retries
- **Rollback as a new, auditable deployment** rather than mutating history
- Deployment events published to RabbitMQ only **after the database transaction commits**, asynchronously
- Full status history per deployment, and a "current version per environment" view
- Custom Micrometer metrics (status transitions, rollbacks) and request-ID correlation in logs (MDC)

### Platform & infrastructure
- Kubernetes manifests and a Helm chart with readiness/liveness probes, resource requests/limits, HPAs, PodDisruptionBudgets, NetworkPolicies, ServiceAccounts, and least-privilege RBAC
- Terraform for AWS: VPC, EKS, ingress-nginx, kube-prometheus-stack, and the platform's own Helm release, with remote state in S3
- Prometheus `ServiceMonitor`, alert rules, and a Grafana dashboard
- GitOps desired-state example for Argo CD / Flux style reconciliation

### Security / DevSecOps
- **OIDC/JWT-protected API** with role-based access (`VIEWER` reads, `OPERATOR` writes); fails closed if no issuer is configured
- **Production guardrails**: immutable image tags, required service owner, deployer identity, and namespace
- **Pod hardening**: `runAsNonRoot`, `seccompProfile: RuntimeDefault`, no privilege escalation, all capabilities dropped
- **CI security gates**: Semgrep SAST on every PR and Grype vulnerability scans of both container images (fails on fixable high/critical CVEs)
- Workflow token scoped to `contents: read`

---

## Why this exists

Kubernetes is powerful but low-level. In many teams:

- deployments are inconsistent and configuration drifts between developers
- there is no single record of what is running where
- incident time is lost just figuring out which version is in production
- one configuration mistake can take down a service

This platform puts a service layer in front of the cluster that standardizes deployments, tracks deployment state centrally, enforces production guardrails, and exposes telemetry for operators.

---

## How it works

1. Register a service and its environments (dev, staging, production).
2. Request a deployment through the API, UI, or CLI.
3. The platform validates deployment policy.
4. The deployment is recorded as `PENDING` with full history.
5. A `deployment.created` event is published to RabbitMQ after commit.
6. A consumer (a deploy worker, or a GitOps commit picked up by Argo CD/Flux) applies the desired state to the cluster and reports status back through `PATCH /v1/deployments/{id}/status`.
7. On success the deployment becomes the current version for that environment.

> The platform is the control plane and source of truth. It does not itself call the Kubernetes API yet; see [Roadmap](#roadmap).

See [docs/architecture.md](docs/architecture.md) for the component diagram, deployment and rollback flows, data model, and tradeoffs. Related: [deployment policies](docs/policies.md), [GitOps integration](docs/gitops.md), [observability](infra/observability/README.md).

---

## Quickstart

### Run everything with Docker Compose

```bash
cp .env.example .env
```

Set `DEVPLATFORM_JWT_ISSUER_URI` in `.env` to your OIDC provider (Okta, Auth0, Keycloak, Cognito, ...), then:

```bash
docker compose up --build
```

| Service | URL |
| --- | --- |
| API | http://localhost:8080 |
| UI | http://localhost:8081 |
| RabbitMQ management | http://localhost:15672 |

### Run the backend tests

Requires JDK 21+.

```bash
mvn test
```

### Build the frontend

```bash
cd frontend && npm ci && npm run build
```

---

## Authentication

The API is an OAuth2 resource server. Every `/v1` request needs an access token from the issuer configured in `DEVPLATFORM_JWT_ISSUER_URI`.

| Role | Access |
| --- | --- |
| `VIEWER` | `GET` on all `/v1` endpoints |
| `OPERATOR` | everything, including create, rollback, status updates, and delete |

Roles are read from the token's `roles` claim. Set `DEVPLATFORM_JWT_ROLES_CLAIM` if your provider uses a different claim. Health and Prometheus endpoints stay open for probes and scraping.

---

## Example API

### Deploy

```http
POST /v1/deployments
Authorization: Bearer <token>
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

### Roll back

```http
POST /v1/deployments/42/rollback
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "deployedBy": "alice"
}
```

### Current deployments

```http
GET /v1/deployments/current?environment=production
Authorization: Bearer <token>
```

---

## CLI

```bash
export DEV_PLATFORM_TOKEN=<access token>
bin/dev-platform services list
bin/dev-platform environments list
bin/dev-platform deploy payments-api --env dev --image payments-api:v1 --by alice
bin/dev-platform deployments current --env production
bin/dev-platform rollback 42 --by alice
```

Set `DEV_PLATFORM_URL` to point the CLI at a non-default API host.

---

## Production guardrails

Environments named `prod` or `production` require:

- an immutable image tag (not `latest`)
- a service owner
- deployer identity
- an environment namespace

These are deliberately small, readable rules that show where platform policy belongs. See [docs/policies.md](docs/policies.md).

---

## CI pipeline

[`.github/workflows/ci.yml`](.github/workflows/ci.yml) runs on every pull request and push to `main`:

| Job | What it checks |
| --- | --- |
| Backend tests | `mvn test`, including authorization tests (401/403 paths) |
| Frontend build | TypeScript compile and Vite build |
| Semgrep SAST | `p/default`, `p/java`, `p/typescript` rule sets; any finding fails the build |
| Platform artifacts | Builds both images, scans them with Grype, lints the Helm chart, and runs `terraform fmt` / `validate` |

An image publishing and EKS deploy workflow (GHCR, multi-arch builds, AWS OIDC role assumption) is drafted in [`publish-images.yml`](.github/workflows/publish-images.yml) and currently disabled.

---

## Deploying to AWS

1. Create an S3 bucket for Terraform state and set it in the `backend "s3"` block in [`infra/terraform/main.tf`](infra/terraform/main.tf).
2. Set `auth.issuerUri` (and real database credentials) in [`infra/helm/dev-platform/values.yaml`](infra/helm/dev-platform/values.yaml).
3. Apply:

```bash
cd infra/terraform && terraform init && terraform apply
```

This provisions the VPC and EKS cluster, installs ingress-nginx and kube-prometheus-stack, and deploys the platform's Helm release.

---

## Repository layout

| Path | Contents |
| --- | --- |
| `src/` | Spring Boot API |
| `frontend/` | React + TypeScript UI |
| `bin/dev-platform` | Shell CLI |
| `infra/k8s` | Static Kubernetes manifests |
| `infra/helm/dev-platform` | Helm chart |
| `infra/terraform` | AWS VPC, EKS, and Helm release wiring |
| `infra/observability` | Prometheus alerts and Grafana dashboard |
| `infra/gitops` | GitOps desired-state example |
| `docs/` | Architecture, policies, GitOps notes |

---

## Limitations

- No deploy worker yet: the platform records and announces deployments, and status is reported back through the API
- The UI has no login flow, so it cannot call the protected API yet
- The schema is managed by Hibernate `ddl-auto=update` rather than versioned migrations
- Database and RabbitMQ credentials in the Helm values are development defaults
- Event publishing is best-effort after commit; there is no transactional outbox
- No production approval workflow

---

## Roadmap

### Backend
- Deploy worker that consumes deployment events and applies them with the Kubernetes Java client under scoped RBAC, or commits desired state for Argo CD
- Flyway migrations in place of `ddl-auto`
- Testcontainers integration tests against real PostgreSQL and RabbitMQ
- Transactional outbox for guaranteed event delivery
- Take `deployedBy` from the authenticated token instead of the request body
- RFC 9457 problem-details errors, pagination, and rate limiting
- OpenTelemetry tracing across API, database, and message bus

### Platform & infrastructure
- Enable the publish/deploy workflow, and add an end-to-end smoke test on a `kind` cluster in CI
- Argo CD application manifests for the platform itself
- Secrets from AWS Secrets Manager via External Secrets Operator and IRSA instead of Helm values
- Per-environment Terraform workspaces or tfvars, plus state locking
- SLOs with burn-rate alerts

### Security / DevSecOps
- Image signing with Cosign, SBOM generation with Syft, and SLSA build provenance
- Admission policy with Kyverno or OPA Gatekeeper that only allows signed images with immutable tags
- Secret scanning (Gitleaks) and IaC scanning (Checkov or Trivy config) in CI
- Dependabot or Renovate for dependency updates, with GitHub Actions pinned to commit SHAs
- Distroless, non-root runtime images
- PR-based production approvals with an audit trail

---

## License

MIT License
