import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { apiRequest } from '../lib/api'

export function Register() {
  const [name, setName] = useState('')
  const [lastName, setLastName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setMessage(null)
    setLoading(true)
    try {
      const res = await apiRequest<null>('/api/auth/register', {
        method: 'POST',
        skipAuth: true,
        body: { name, lastName, email: email.trim(), password },
      })
      setMessage(res.message || 'Revisa tu correo para confirmar la cuenta.')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo registrar')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page narrow">
      <div className="card">
        <h1>Crear cuenta</h1>
        <p className="muted">
          ¿Ya tienes cuenta? <Link to="/login">Inicia sesión</Link>
        </p>
        <form className="form" onSubmit={onSubmit}>
          {error ? <p className="alert error">{error}</p> : null}
          {message ? <p className="alert success">{message}</p> : null}
          <label>
            Nombre
            <input value={name} onChange={(e) => setName(e.target.value)} required />
          </label>
          <label>
            Apellido
            <input value={lastName} onChange={(e) => setLastName(e.target.value)} required />
          </label>
          <label>
            Correo
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </label>
          <label>
            Contraseña (mín. 6 caracteres)
            <input
              type="password"
              minLength={6}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </label>
          <button type="submit" className="btn primary full" disabled={loading}>
            {loading ? 'Enviando…' : 'Registrarme'}
          </button>
        </form>
      </div>
    </div>
  )
}
