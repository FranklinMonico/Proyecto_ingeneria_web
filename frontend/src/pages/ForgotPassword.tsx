import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { apiRequest } from '../lib/api'

export function ForgotPassword() {
  const [email, setEmail] = useState('')
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setMessage(null)
    setLoading(true)
    try {
      const res = await apiRequest<null>(
        `/api/auth/forgot-password?email=${encodeURIComponent(email.trim())}`,
        { method: 'POST', skipAuth: true },
      )
      setMessage(res.message)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al solicitar el enlace')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page narrow">
      <div className="card">
        <h1>Recuperar contraseña</h1>
        <p className="muted">Te enviaremos un enlace si el correo existe en el sistema.</p>
        <form className="form" onSubmit={onSubmit}>
          {error ? <p className="alert error">{error}</p> : null}
          {message ? <p className="alert success">{message}</p> : null}
          <label>
            Correo
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </label>
          <button type="submit" className="btn primary full" disabled={loading}>
            {loading ? 'Enviando…' : 'Enviar enlace'}
          </button>
          <p className="muted small">
            <Link to="/login">Volver al inicio de sesión</Link>
          </p>
        </form>
      </div>
    </div>
  )
}
