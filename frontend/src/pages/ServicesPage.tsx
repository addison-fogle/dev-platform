import { useState, useEffect } from 'react'
import { api } from '../api'
import type { Service } from '../types'

export default function ServicesPage() {
  const [services, setServices] = useState<Service[]>([])
  const [error, setError] = useState('')
  const [name, setName] = useState('')
  const [owner, setOwner] = useState('')
  const [imageRegistry, setImageRegistry] = useState('')

  useEffect(() => { load() }, [])

  async function load() {
    try { setServices(await api.services.list()) }
    catch (e) { setError(String(e)) }
  }

  async function create(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    try {
      await api.services.create({ name, owner: owner || null, imageRegistry: imageRegistry || null })
      setName(''); setOwner(''); setImageRegistry('')
      load()
    } catch (e) { setError(String(e)) }
  }

  async function remove(id: number) {
    setError('')
    try { await api.services.delete(id); load() }
    catch (e) { setError(String(e)) }
  }

  return (
    <>
      <h1>Services</h1>
      {error && <div className="error">{error}</div>}
      <div className="card">
        <form className="form-row" onSubmit={create}>
          <div className="field">
            <label>Name *</label>
            <input value={name} onChange={e => setName(e.target.value)} required placeholder="my-service" style={{ width: 160 }} />
          </div>
          <div className="field">
            <label>Owner</label>
            <input value={owner} onChange={e => setOwner(e.target.value)} placeholder="team-name" style={{ width: 140 }} />
          </div>
          <div className="field">
            <label>Image Registry</label>
            <input value={imageRegistry} onChange={e => setImageRegistry(e.target.value)} placeholder="registry.io/org" style={{ width: 200 }} />
          </div>
          <button type="submit" className="btn-primary">Add Service</button>
        </form>
      </div>
      <div className="card" style={{ padding: 0 }}>
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Owner</th>
              <th>Image Registry</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {services.length === 0 ? (
              <tr><td colSpan={4} className="empty">No services yet</td></tr>
            ) : services.map(s => (
              <tr key={s.id}>
                <td><strong>{s.name}</strong></td>
                <td>{s.owner ?? <span style={{ color: '#cbd5e1' }}>—</span>}</td>
                <td><code>{s.imageRegistry ?? <span style={{ color: '#cbd5e1' }}>—</span>}</code></td>
                <td style={{ width: 80 }}>
                  <button className="btn-danger btn-sm" onClick={() => remove(s.id)}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  )
}
