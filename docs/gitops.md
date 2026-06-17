# GitOps Integration

The API can be extended to write desired deployment state to a Git repository instead of applying manifests directly. A GitOps controller such as Argo CD or Flux would then reconcile the cluster from that repository.

## Flow

1. Developer requests a deployment.
2. Platform validates service, environment, and policy.
3. Platform writes an environment-specific values file.
4. Platform opens a pull request or commits to an approved branch.
5. Argo CD or Flux reconciles the generated desired state.
6. Platform tracks status through controller webhooks or Kubernetes watch events.

## Example Generated File

See `infra/gitops/services/payments-api-production.yaml`.

## Benefits

- auditable deployment changes
- normal code review for production state
- easy drift detection
- platform API stays focused on workflow and policy
- cluster reconciliation is delegated to a proven controller

## Tradeoffs

- slower deploy path if pull requests are required
- status reporting needs controller integration
- rollback can be modeled as another Git commit rather than an imperative cluster change
