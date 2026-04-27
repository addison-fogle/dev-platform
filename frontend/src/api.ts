import type { Service, Environment, Deployment, HistoryEntry, DeploymentRequest, DeploymentStatus } from './types'

const BASE = '/v1'

async function req<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(BASE + path, init)
  if (!res.ok) throw new Error(await res.text())
  if (res.status === 204) return undefined as T
  return res.json() as Promise<T>
}

const json = (body: unknown) => ({
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(body),
})

export const api = {
  services: {
    list: () => req<Service[]>('/services'),
    create: (data: Omit<Service, 'id'>) => req<Service>('/services', { method: 'POST', ...json(data) }),
    delete: (id: number) => req<void>(`/services/${id}`, { method: 'DELETE' }),
  },
  environments: {
    list: () => req<Environment[]>('/environments'),
    create: (data: Omit<Environment, 'id'>) => req<Environment>('/environments', { method: 'POST', ...json(data) }),
    delete: (id: number) => req<void>(`/environments/${id}`, { method: 'DELETE' }),
  },
  deployments: {
    list: () => req<Deployment[]>('/deployments'),
    create: (data: DeploymentRequest) => req<Deployment>('/deployments', { method: 'POST', ...json(data) }),
    updateStatus: (id: number, status: DeploymentStatus) =>
      req<Deployment>(`/deployments/${id}/status`, { method: 'PATCH', ...json(status) }),
    history: (id: number) => req<HistoryEntry[]>(`/deployments/${id}/history`),
  },
}
