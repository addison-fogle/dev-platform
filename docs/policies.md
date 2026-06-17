# Deployment Policies

The platform has a small policy layer to demonstrate how an internal developer platform can improve safety without making every team hand-author Kubernetes rules.

## Current Rules

Production environments are identified by the names `prod` or `production`.

For production deployments:

- `latest` and `*:latest` image tags are rejected
- service owner is required
- `deployedBy` is required
- environment namespace is required

## Suggested Extensions

- require approvals for production
- restrict deployments to registered image registries
- require signed images
- validate image digest availability before deploy
- enforce maintenance windows for selected environments
- block deploys during active incidents

## Why This Matters

Platform engineering is not just deployment automation. It is also guardrails, defaults, and paved paths that let product teams move quickly without bypassing operational safety.
