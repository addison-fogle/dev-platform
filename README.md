# development-platform
A lightweight platform that standardizes how services are deployed and managed on Kubernetes.

Instead of manually writing YAML and running kubectl, this system provides a simple API to register services, deploy versions, and track what’s running across environments.

---

## Why this exists

Kubernetes is powerful but low-level. In many teams:
- deployments are inconsistent  
- configurations vary between developers  
- there is no clear record of what is running where  

This project introduces a thin abstraction layer to:
- standardize deployments  
- simplify developer workflows  
- track deployment state centrally  

---

## What it does

- Register services and environments  
- Deploy container images to Kubernetes  
- Store deployment history and status  
- Provide a consistent “golden path” for deployments  

Example:

deploy payments-api:v3 to dev

---

## Architecture

- Backend: Spring Boot  
- Database: PostgreSQL  
- Orchestration: Kubernetes  
- Infra: AWS  

Core components:
- Service registry (source of truth)
- Deployment service (handles deploy requests)
- Kubernetes integration layer (applies manifests)
- Database (tracks services, environments, deployments)

---

## How it works

1. Register a service  
2. Define environment configs (dev, staging, prod)  
3. Trigger a deployment via API  
4. Platform generates Kubernetes manifests  
5. Applies them to the cluster  
6. Tracks deployment status and history  

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
