export type DeploymentStatus = 'PENDING' | 'RUNNING' | 'SUCCEEDED' | 'FAILED'

export interface Service {
  id: number
  name: string
  owner: string | null
  imageRegistry: string | null
}

export interface Environment {
  id: number
  name: string
  namespace: string | null
  clusterContext: string | null
}

export interface Deployment {
  id: number
  service: Service
  environment: Environment
  imageTag: string
  status: DeploymentStatus
  deployedBy: string | null
  current: boolean
  createdAt: string
  updatedAt: string
}

export interface HistoryEntry {
  id: number
  fromStatus: DeploymentStatus | null
  toStatus: DeploymentStatus
  changedAt: string
}

export interface DeploymentRequest {
  serviceName: string
  environment: string
  imageTag: string
  deployedBy: string
}
