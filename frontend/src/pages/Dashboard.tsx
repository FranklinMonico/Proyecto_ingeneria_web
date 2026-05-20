import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { API_BASE } from '../config'
import { apiRequest, assetUrl, downloadCertificatePdf } from '../lib/api'
import { useAuth } from '../context/AuthContext'
import type { AvailableCourse, Certificate, CourseProgress, DashboardData } from '../types/api'

export function Dashboard() {
  const { email } = useAuth()
  const [data, setData] = useState<DashboardData | null>(null)
  const [certs, setCerts] = useState<Certificate[]>([])
  const [error, setError] = useState<string | null>(null)
  const [progressMap, setProgressMap] = useState<Record<string, CourseProgress | null>>({})
  const [loadingProgress, setLoadingProgress] = useState<string | null>(null)

  const load = useCallback(async () => {
    setError(null)
    try {
      const dash = await apiRequest<DashboardData>('/api/dashboard')
      setData(dash.data)
      const em = dash.data?.email ?? email
      if (em) {
        const listRes = await fetch(`${API_BASE}/api/certificates/${encodeURIComponent(em)}`)
        if (listRes.ok) {
          setCerts((await listRes.json()) as Certificate[])
        }
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo cargar el panel')
    }
  }, [email])

  useEffect(() => {
    void load()
  }, [load])

  async function loadProgress(courseId: string) {
    setLoadingProgress(courseId)
    try {
      const res = await apiRequest<CourseProgress>(`/api/progress/${encodeURIComponent(courseId)}`)
      setProgressMap((m) => ({ ...m, [courseId]: res.data }))
    } catch {
      setProgressMap((m) => ({ ...m, [courseId]: null }))
    } finally {
      setLoadingProgress(null)
    }
  }

  if (error) {
    return (
      <div className="page">
        <p className="alert error">{error}</p>
        <button type="button" className="btn secondary" onClick={() => void load()}>
          Reintentar
        </button>
      </div>
    )
  }

  if (!data) {
    return (
      <div className="page">
        <p className="muted">Cargando panel…</p>
      </div>
    )
  }

  const inProgress = data.enrolledCourses.filter((course) => !course.completed).length

  return (
    <div className="page">
      <header className="dash-header">
        <div>
          <p className="dash-greeting">Hola, {data.name}</p>
          <p className="dash-title">Tu panel</p>
          <p className="dash-email-sub">{data.email}</p>
        </div>
        <div className="stat-cards">
          <article className="stat-card">
            <p className="stat-card-label">Inscritos</p>
            <p className="stat-card-value">{data.enrolledCourses.length}</p>
          </article>
          <article className="stat-card">
            <p className="stat-card-label">En curso</p>
            <p className="stat-card-value">{inProgress}</p>
          </article>
          <article className="stat-card">
            <p className="stat-card-label">Certificados</p>
            <p className="stat-card-value">{data.totalCertificates}</p>
          </article>
        </div>
      </header>

      <section className="section">
        <div className="section-header">
          <h2 className="section-title">Cursos inscritos</h2>
        </div>
        {data.enrolledCourses.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon" aria-hidden>
              📘
            </div>
            <div className="empty-text">
              <p>Aún no tienes cursos inscritos</p>
              <p>Explora el catálogo y empieza a aprender hoy mismo.</p>
            </div>
          </div>
        ) : (
          <div className="grid cards">
            {data.enrolledCourses.map((c) => (
              <article key={c.courseId} className="card course-card">
                <h3>{c.courseName}</h3>

                <div className="progress-bar" aria-label="Progreso">
                  <div
                    className="progress-fill"
                    style={{
                      width: `${c.totalModules ? Math.min(100, (c.progress / c.totalModules) * 100) : 0}%`,
                    }}
                  />
                </div>
                <p className="small">
                  Módulos: {c.progress}/{c.totalModules}
                  {c.completed ? <span className="badge ok">Completado</span> : null}
                </p>
                <div className="row gap wrap">
                  <Link to={`/forum/${encodeURIComponent(c.courseId)}`} className="btn secondary small">
                    Foro
                  </Link>

                </div>

              </article>
            ))}
          </div>
        )}
      </section>

      <section className="section">
        <div className="section-header">
          <h2 className="section-title">Catálogo disponible</h2>
        </div>
        {data.availableCourses.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon" aria-hidden>
              🗂️
            </div>
            <div className="empty-text">
              <p>No hay cursos disponibles</p>
              <p>Vuelve más tarde para descubrir nuevo contenido.</p>
            </div>
          </div>
        ) : (
          <div className="grid cards">
            {data.availableCourses.map((c: AvailableCourse) => (
              <article key={c.courseId} className="card course-card">
                {c.imageUrl ? (
                  <img className="thumb" src={assetUrl(c.imageUrl)} alt="" loading="lazy" />
                ) : null}
                <h3>{c.title}</h3>
                <p className="muted small">{c.description}</p>
                <p className="small">
                  Instructor: {c.instructor} · {c.totalModules} módulos · {c.totalLessons} lecciones
                </p>
                <p className="price">{c.price === 0 ? 'Gratis' : `${c.price.toFixed(2)} €`}</p>
                {c.enrolled ? (
                  <span className="badge">Inscrito</span>
                ) : (
                  <p className="muted small">La inscripción se gestiona desde el motor de aprendizaje.</p>
                )}
              </article>
            ))}
          </div>
        )}
      </section>

      <section className="section">
        <div className="section-header">
          <h2 className="section-title">Mis certificados</h2>
        </div>
        {certs.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon" aria-hidden>
              🏅
            </div>
            <div className="empty-text">
              <p>No hay certificados registrados</p>
              <p>Completa un curso para obtener tu primer certificado.</p>
            </div>
          </div>
        ) : (
          <ul className="list">
            {certs.map((cert) => (
              <li key={cert.id} className="list-row">
                <div>
                  <strong>{cert.courseName}</strong>
                  <p className="muted small">
                    Emitido: {new Date(cert.issuedAt).toLocaleString()}
                    {cert.sent ? <span className="badge ok">Enviado</span> : null}
                  </p>
                </div>
                <button
                  type="button"
                  className="btn secondary small"
                  onClick={() => void downloadCertificatePdf(cert.id)}
                >
                  PDF
                </button>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  )
}
