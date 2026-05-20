import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { API_BASE } from '../config'
import { apiRequest, assetUrl, getStoredAccessToken } from '../lib/api'
import type { StudentProfile } from '../types/api'

export function Profile() {
  const [student, setStudent] = useState<StudentProfile | null>(null)
  const [name, setName] = useState('')
  const [lastName, setLastName] = useState('')
  const [pictureUrl, setPictureUrl] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [crmMsg, setCrmMsg] = useState<string | null>(null)
  const [avatarBlobUrl, setAvatarBlobUrl] = useState<string | null>(null)

  const load = useCallback(async () => {
    setError(null)
    try {
      const res = await apiRequest<StudentProfile>('/api/profile')
      const s = res.data
      if (!s) return
      setStudent(s)
      setName(s.name)
      setLastName(s.lastName ?? '')
      setPictureUrl(s.profilePicture ?? null)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo cargar el perfil')
    }
  }, [])

  // ✅ Este faltaba — dispara la carga del perfil al montar
  useEffect(() => {
    void load()
  }, [load])

  // ✅ Carga el avatar con el token correcto
  useEffect(() => {
    if (!pictureUrl) {
      setAvatarBlobUrl(null)
      return
    }

    let objectUrl: string | null = null

    const loadImage = async () => {
      try {
        const token = getStoredAccessToken() // usa la misma función que el resto del app
        const url = assetUrl(pictureUrl)
        const res = await fetch(url, {
          headers: token ? { Authorization: `Bearer ${token}` } : {}
        })
        if (!res.ok) throw new Error(`${res.status}`)
        const blob = await res.blob()
        objectUrl = URL.createObjectURL(blob)
        setAvatarBlobUrl(objectUrl)
      } catch (err) {
        console.error('No se pudo cargar el avatar:', err)
        setAvatarBlobUrl(null)
      }
    }

    void loadImage()

    return () => {
      if (objectUrl) URL.revokeObjectURL(objectUrl)
    }
  }, [pictureUrl])

  // ✅ onUpload limpio, sin código extra pegado arriba
  async function onUpload(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file) return
    setError(null)
    setMessage(null)
    const fd = new FormData()
    fd.append('file', file)
    const token = getStoredAccessToken()
    try {
      const res = await fetch(`${API_BASE}/api/upload/profile-picture`, {
        method: 'POST',
        headers: token ? { Authorization: `Bearer ${token}` } : {},
        body: fd,
      })
      const json = (await res.json()) as { success: boolean; message: string; data: string | null }
      if (!res.ok || !json.success || !json.data) {
        throw new Error(json.message || 'Error al subir la imagen')
      }
      setPictureUrl(json.data)
      setMessage('Imagen subida. Guarda el perfil para aplicar la foto.')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al subir')
    } finally {
      e.target.value = ''
    }
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setMessage(null)
    setLoading(true)
    try {
      const res = await apiRequest<StudentProfile>('/api/profile', {
        method: 'PUT',
        body: {
          name,
          lastName,
          profilePicture: pictureUrl ?? undefined,
        },
      })
      if (res.data) setStudent(res.data)
      setMessage(res.message || 'Perfil actualizado')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo guardar')
    } finally {
      setLoading(false)
    }
  }

  async function syncCrm() {
    setCrmMsg(null)
    setError(null)
    try {
      const res = await apiRequest<null>('/api/crm/sync', { method: 'POST' })
      setCrmMsg(res.message)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al sincronizar')
    }
  }

  if (!student && !error) {
    return (
      <div className="page">
        <p className="muted">Cargando perfil…</p>
      </div>
    )
  }

  return (
    <div className="page narrow">
      <h1>Perfil</h1>
      {error ? <p className="alert error">{error}</p> : null}
      {message ? <p className="alert success">{message}</p> : null}
      {crmMsg ? <p className="alert success">{crmMsg}</p> : null}

      <div className="card profile-card">
        <div className="avatar-wrap">
          {avatarBlobUrl ? (
            <img className="avatar" src={avatarBlobUrl} alt="Foto de perfil" />
          ) : (
            <div
              className="avatar placeholder"
              style={{
                background: '#1d4ed8', color: 'white',
                display: 'flex', alignItems: 'center',
                justifyContent: 'center', fontSize: 32,
                fontWeight: 700
              }}
            >
              {(student?.name ?? '?').charAt(0).toUpperCase()}
            </div>
          )}
          <label className="btn secondary small" style={{ cursor: 'pointer' }}>
            {pictureUrl ? 'Cambiar foto' : 'Subir foto'}
            <input type="file" accept="image/*" hidden onChange={onUpload} />
          </label>
        </div>

        <form className="form" onSubmit={onSubmit}>
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
            <input value={student?.email ?? ''} disabled className="disabled" />
          </label>

          <div className="row gap wrap">
            <button type="submit" className="btn primary" disabled={loading}>
              {loading ? 'Guardando…' : 'Guardar cambios'}
            </button>
            <button type="button" className="btn secondary" onClick={() => void syncCrm()}>
              Sincronizar con EspoCRM
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}