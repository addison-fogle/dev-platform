import { BrowserRouter, NavLink, Route, Routes, Navigate } from 'react-router-dom'
import ServicesPage from './pages/ServicesPage'
import EnvironmentsPage from './pages/EnvironmentsPage'
import DeploymentsPage from './pages/DeploymentsPage'

export default function App() {
  return (
    <BrowserRouter>
      <div className="layout">
        <nav>
          <div className="nav-logo">Dev Platform</div>
          <NavLink to="/deployments" className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}>
            Deployments
          </NavLink>
          <NavLink to="/services" className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}>
            Services
          </NavLink>
          <NavLink to="/environments" className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}>
            Environments
          </NavLink>
        </nav>
        <main>
          <Routes>
            <Route path="/" element={<Navigate to="/deployments" replace />} />
            <Route path="/deployments" element={<DeploymentsPage />} />
            <Route path="/services" element={<ServicesPage />} />
            <Route path="/environments" element={<EnvironmentsPage />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  )
}
