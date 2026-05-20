import { useState, type FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from = (location.state as { from?: string } | null)?.from ?? '/dashboard'

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await login(email.trim(), password)
      navigate(from, { replace: true })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al iniciar sesión')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-screen">
      <div className="login-card">
        <aside className="login-left">
          <div className="login-logo">
            <span className="login-logo-icon" aria-hidden>
              🎓
            </span>
            <span className="login-logo-name">Aula digital</span>
          </div>

          <div className="login-left-headline">
            <h2>Aprende a tu ritmo, desde cualquier lugar</h2>
            <p>
              Accede a cursos certificados y avanza en tu carrera profesional con instructores expertos.
            </p>
          </div>

          <div className="login-perks">
            <div className="login-perk">
              <span className="perk-icon" aria-hidden>
                ✓
              </span>
              <span>Certificados verificados al completar</span>
            </div>
            <div className="login-perk">
              <span className="perk-icon" aria-hidden>
                📚
              </span>
              <span>+200 cursos en todas las áreas</span>
            </div>
            <div className="login-perk">
              <span className="perk-icon" aria-hidden>
                ⚡
              </span>
              <span>Aprende a tu propio ritmo</span>
            </div>
          </div>
        </aside>

        <section className="login-right">
          <h1>Iniciar sesión</h1>
          <p className="login-sub">
            ¿No tienes cuenta? <Link to="/register">Regístrate</Link>
          </p>
          <form className="form" onSubmit={onSubmit}>
            {error ? <p className="alert error">{error}</p> : null}
            <div className="form-group">
              <label>Correo electrónico</label>
              <input
                type="email"
                autoComplete="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="nombre@correo.com"
                required
              />
            </div>
            <div className="form-group">
              <label>Contraseña</label>
              <input
                type="password"
                autoComplete="current-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                required
              />
            </div>
            <Link to="/forgot-password" className="forgot-link">
              ¿Olvidaste tu contraseña?
            </Link>
            <button type="submit" className="btn primary full" disabled={loading}>
              {loading ? 'Entrando…' : 'Entrar'}
            </button>
          </form>
        </section>
      </div>
    </div>
  )
}
