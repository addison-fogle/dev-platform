import { useState, useEffect } from 'react'
import { api } from '../api'
import type { Environment } from '../types'

export default function EnvironmentsPage() {
  const [environments, setEnvironments] = useState<Environment[]>([])
  const [error, setError] = useState('')
  const [name, setName] = useState('')
  const [namespace, setNamespace] = useState('')
  const [clusterContext, setClusterContext] = useState('')

  useEffect(() => { load() }, [])

  async function load() {
    try { setEnvironments(await api.environments.list()) }
    catch (e) { setError(String(e)) }
  }

  async function create(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    try {
      await api.environments.create({ name, namespace: namespace || null, clusterContext: clusterContext || null })
      setName(''); setNamespace(''); setClusterContext('')
      load()
    } catch (e) { setError(String(e)) }
  }

  async function remove(id: number) {
    setError('')
    try { await api.environments.delete(id); load() }
    catch (e) { setError(String(e)) }
  }

  return (
    <>
      <h1>Environments</h1>
      {error && <div className="error">{error}</div>}
      <div className="card">
        <form className="form-row" onSubmit={create}>
          <div className="field">
            <label>Name *</label>
            <input value={name} onChange={e => setName(e.target.value)} required placeholder="production" style={{ width: 140 }} />
          </div>
          <div className="field">
            <label>Namespace</label>
            <input value={namespace} onChange={e => setNamespace(e.target.value)} placeholder="default" style={{ width: 140 }} />
          </div>
          <div className="field">
            <label>Cluster Context</label>
            <input value={clusterContext} onChange={e => setClusterContext(e.target.value)} placeholder="gke_prod_us-east1" style={{ width: 220 }} />
          </div>
          <button type="submit" className="btn-primary">Add Environment</button>
        </form>
      </div>
      <div className="card" style={{ padding: 0 }}>
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Namespace</th>
              <th>Cluster Context</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {environments.length === 0 ? (
              <tr><td colSpan={4} className="empty">No environments yet</td></tr>
            ) : environments.map(env => (
              <tr key={env.id}>
                <td><strong>{env.name}</strong></td>
                <td><code>{env.namespace ?? <span style={{ color: '#cbd5e1' }}>—</span>}</code></td>
                <td><code>{env.clusterContext ?? <span style={{ color: '#cbd5e1' }}>—</span>}</code></td>
                <td style={{ width: 80 }}>
                  <button className="btn-danger btn-sm" onClick={() => remove(env.id)}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  )
}
