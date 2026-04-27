import { useState, useEffect, Fragment } from 'react'
import { api } from '../api'
import type { Deployment, DeploymentStatus, HistoryEntry } from '../types'

const STATUSES: DeploymentStatus[] = ['PENDING', 'RUNNING', 'SUCCEEDED', 'FAILED']

export default function DeploymentsPage() {
  const [deployments, setDeployments] = useState<Deployment[]>([])
  const [error, setError] = useState('')
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [history, setHistory] = useState<HistoryEntry[]>([])

  const [serviceName, setServiceName] = useState('')
  const [environment, setEnvironment] = useState('')
  const [imageTag, setImageTag] = useState('')
  const [deployedBy, setDeployedBy] = useState('')

  const [pendingStatus, setPendingStatus] = useState<Record<number, DeploymentStatus>>({})

  useEffect(() => { load() }, [])

  async function load() {
    try { setDeployments(await api.deployments.list()) }
    catch (e) { setError(String(e)) }
  }

  async function create(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    try {
      await api.deployments.create({ serviceName, environment, imageTag, deployedBy })
      setServiceName(''); setEnvironment(''); setImageTag(''); setDeployedBy('')
      load()
    } catch (e) { setError(String(e)) }
  }

  async function applyStatus(id: number) {
    const status = pendingStatus[id]
    if (!status) return
    setError('')
    try {
      await api.deployments.updateStatus(id, status)
      setPendingStatus(prev => { const next = { ...prev }; delete next[id]; return next })
      load()
      if (selectedId === id) loadHistory(id)
    } catch (e) { setError(String(e)) }
  }

  async function loadHistory(id: number) {
    if (selectedId === id) { setSelectedId(null); return }
    try {
      const h = await api.deployments.history(id)
      setHistory(h)
      setSelectedId(id)
    } catch (e) { setError(String(e)) }
  }

  return (
    <>
      <h1>Deployments</h1>
      {error && <div className="error">{error}</div>}
      <div className="card">
        <form className="form-row" onSubmit={create}>
          <div className="field">
            <label>Service *</label>
            <input value={serviceName} onChange={e => setServiceName(e.target.value)} required placeholder="my-service" style={{ width: 150 }} />
          </div>
          <div className="field">
            <label>Environment *</label>
            <input value={environment} onChange={e => setEnvironment(e.target.value)} required placeholder="production" style={{ width: 130 }} />
          </div>
          <div className="field">
            <label>Image Tag *</label>
            <input value={imageTag} onChange={e => setImageTag(e.target.value)} required placeholder="v1.2.3" style={{ width: 110 }} />
          </div>
          <div className="field">
            <label>Deployed By</label>
            <input value={deployedBy} onChange={e => setDeployedBy(e.target.value)} placeholder="username" style={{ width: 120 }} />
          </div>
          <button type="submit" className="btn-primary">Deploy</button>
        </form>
      </div>
      <div className="card" style={{ padding: 0 }}>
        <table>
          <thead>
            <tr>
              <th>Service</th>
              <th>Environment</th>
              <th>Image Tag</th>
              <th>Status</th>
              <th>Current</th>
              <th>Deployed By</th>
              <th>Created</th>
              <th>Update Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {deployments.length === 0 ? (
              <tr><td colSpan={9} className="empty">No deployments yet</td></tr>
            ) : deployments.map(d => (
              <Fragment key={d.id}>
                <tr>
                  <td><strong>{d.service.name}</strong></td>
                  <td>{d.environment.name}</td>
                  <td><code>{d.imageTag}</code></td>
                  <td><span className={`badge badge-${d.status}`}>{d.status}</span></td>
                  <td>{d.current && <span className="current-dot" title="Current" />}</td>
                  <td>{d.deployedBy ?? <span style={{ color: '#cbd5e1' }}>—</span>}</td>
                  <td style={{ whiteSpace: 'nowrap', color: '#64748b' }}>
                    {new Date(d.createdAt).toLocaleString()}
                  </td>
                  <td style={{ whiteSpace: 'nowrap' }}>
                    <span style={{ display: 'flex', gap: '0.4rem', alignItems: 'center' }}>
                      <select
                        value={pendingStatus[d.id] ?? d.status}
                        onChange={e => setPendingStatus(prev => ({ ...prev, [d.id]: e.target.value as DeploymentStatus }))}
                        style={{ height: '1.625rem', fontSize: '0.75rem' }}
                      >
                        {STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
                      </select>
                      <button
                        className="btn-ghost btn-sm"
                        onClick={() => applyStatus(d.id)}
                        disabled={!pendingStatus[d.id] || pendingStatus[d.id] === d.status}
                      >
                        Apply
                      </button>
                    </span>
                  </td>
                  <td>
                    <button
                      className={`btn-ghost btn-sm${selectedId === d.id ? ' active' : ''}`}
                      onClick={() => loadHistory(d.id)}
                    >
                      History
                    </button>
                  </td>
                </tr>
                {selectedId === d.id && (
                  <tr>
                    <td colSpan={9} style={{ padding: 0 }}>
                      <div className="history-panel">
                        <h3>History — {d.service.name} @ {d.environment.name}</h3>
                        {history.length === 0 ? (
                          <div style={{ color: '#94a3b8', fontSize: '0.8rem' }}>No history recorded</div>
                        ) : (
                          <table className="history-table">
                            <thead>
                              <tr>
                                <th>From</th>
                                <th>To</th>
                                <th>Changed At</th>
                              </tr>
                            </thead>
                            <tbody>
                              {history.map(h => (
                                <tr key={h.id}>
                                  <td>
                                    {h.fromStatus
                                      ? <span className={`badge badge-${h.fromStatus}`}>{h.fromStatus}</span>
                                      : <span style={{ color: '#cbd5e1' }}>—</span>}
                                  </td>
                                  <td><span className={`badge badge-${h.toStatus}`}>{h.toStatus}</span></td>
                                  <td style={{ color: '#64748b' }}>{new Date(h.changedAt).toLocaleString()}</td>
                                </tr>
                              ))}
                            </tbody>
                          </table>
                        )}
                      </div>
                    </td>
                  </tr>
                )}
              </Fragment>
            ))}
          </tbody>
        </table>
      </div>
    </>
  )
}
