# development-platform
A lightweight platform that standardizes how services are deployed and managed on Kubernetes.

Has your team ever had a production outage and spent too much time on discovering which version is deployed, what's on production, and what the previous verison was? Have they dealt with managing numerous different deployments and services, each requiring different or complex Kubernetes configurations or services setup? I'm creating this development platform so you and your team no longer have to worry about these things.

---

## Why this exists

Kubernetes is powerful but low-level. In many teams:
- deployments are inconsistent  
- configurations vary between developers  
- there is no clear record of what is running where
- teams don't know extactly what version is on production
- too much time is spent during outages on understanding deployment versions

This project introduces a thin abstraction layer to:
- standardize deployments  
- simplify developer workflows  
- track deployment state centrally

---

## What it does

- Register services and environments  
- Deploy containers to Kubernetes  
- Store deployment history and status  
- Provide a consistent path for deployments
- Easily know what servces and versions are on production

Example:

deploy payments-api:v3 to dev

---

## Architecture

- Backend: Spring Boot  
- Database: PostgreSQL  
- Orchestration: Kubernetes  
- Infra: AWS
- Monitoring: Prometheus 

Core components:
- Service registry (source of truth)
- Deployment service (handles deploy requests)
- Kubernetes integration layer (applies manifests)
- Database (tracks services, environments, deployments)
- Simpe UI

---

## How it works

1. Register a service  
2. Define environment configs (dev, staging, prod)  
3. Trigger a deployment via API  
4. Platform generates Kubernetes manifests  
5. Applies them to the cluster  
6. Tracks deployment status and history
7. Creates easy visuals and tracking in UI

---

## Example API

### Create service
POST /services

### Deploy
POST /deployments

json {   "service": "payments-api",   "environment": "dev",   "image": "payments-api:v1" } 

---

## Limitations

- No CI/CD pipeline 
- Minimal authentication  

---

## Future Improvements

- CLI for developers  
- Rollbacks and version management  
- Observability (metrics, logs, tracing)  
- Git-based deployment integration  

## License

MIT License
