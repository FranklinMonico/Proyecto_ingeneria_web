import { useEffect, useState } from 'react'
import { Link, NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { apiRequest, assetUrl, getStoredAccessToken } from '../lib/api'
import type { StudentProfile } from '../types/api'

export function AppLayout() {
  const { email, logout } = useAuth()
  const [avatarBlobUrl, setAvatarBlobUrl] = useState<string | null>(null)

  const initials = (email ?? 'US')
    .split('@')[0]
    .split(/[.\-_]/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() ?? '')
    .join('')

  useEffect(() => {
    let objectUrl: string | null = null

    const loadAvatar = async () => {
      try {
        const res = await apiRequest<StudentProfile>('/api/profile')
        const pictureUrl = res.data?.profilePicture
        if (!pictureUrl) return

        const token = getStoredAccessToken()
        const fetchRes = await fetch(assetUrl(pictureUrl), {
          headers: token ? { Authorization: `Bearer ${token}` } : {}
        })
        if (!fetchRes.ok) return
        const blob = await fetchRes.blob()
        objectUrl = URL.createObjectURL(blob)
        setAvatarBlobUrl(objectUrl)
      } catch {
        // silencioso — si falla muestra iniciales
      }
    }

    void loadAvatar()

    return () => {
      if (objectUrl) URL.revokeObjectURL(objectUrl)
    }
  }, [email]) // se recarga si cambia el usuario

  return (
    <div className="shell">
      <header className="navbar">
        <div className="navbar-left">
          <Link to="/dashboard" className="nav-logo">
            <span className="nav-logo-icon" aria-hidden>🎓</span>
            <span className="nav-logo-name">Aula digital</span>
          </Link>
          <nav className="nav-links">
            <NavLink to="/dashboard" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              Panel
            </NavLink>
            <NavLink to="/profile" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              Perfil
            </NavLink>
          </nav>
        </div>
        <div className="navbar-right">
          <span className="nav-email" title={email ?? ''}>{email}</span>

          {/* Avatar: foto si existe, iniciales si no */}
          {avatarBlobUrl ? (
            <img
              src={avatarBlobUrl}
              alt="Avatar"
              className="nav-avatar"
              style={{ borderRadius: '50%', objectFit: 'cover', width: 36, height: 36 }}
            />
          ) : (
            <span className="nav-avatar" aria-hidden>
              {initials || 'US'}
            </span>
          )}

          <button type="button" className="nav-logout" onClick={logout}>
            Salir
          </button>
        </div>
      </header>
      <main className="main">
        <Outlet />
      </main>
    </div>
  )
}