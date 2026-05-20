import { Link, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export function GuestShell() {
  const { isAuthenticated } = useAuth()
  const { pathname } = useLocation()
  const hideHeader = pathname === '/login'

  return (
    <div className="shell guest">
      {hideHeader ? null : (
        <header className="navbar">
          <div className="navbar-left">
            <Link to="/" className="nav-logo">
              <span className="nav-logo-icon" aria-hidden>
                🎓
              </span>
              <span className="nav-logo-name">Aula digital</span>
            </Link>
          </div>
          <nav className="navbar-right">
            {isAuthenticated ? (
              <Link to="/dashboard" className="nav-logout">
                Panel
              </Link>
            ) : (
              <>
                <Link to="/login" className="nav-link">
                  Entrar
                </Link>
                <Link to="/register" className="btn primary small">
                  Registro
                </Link>
              </>
            )}
          </nav>
        </header>
      )}
      <main className={`main guest-main ${hideHeader ? 'login-main' : ''}`}>
        <Outlet />
      </main>
    </div>
  )
}
