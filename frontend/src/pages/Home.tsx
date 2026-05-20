import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export function Home() {
  const { isAuthenticated } = useAuth()

  return (
    <div className="page hero-page">
      <div className="hero-layout">
        <div className="hero-copy">
          <p className="eyebrow">Campus virtual</p>
          <h1>Tu aula digital: cursos, comunidad y logros en un solo lugar</h1>
          <p className="lede">
            Consulta tu progreso por módulo, participa en los foros de cada asignatura y descarga tus
            certificados cuando completes un recorrido formativo.
          </p>
          <div className="hero-actions">
            {isAuthenticated ? (
              <Link to="/dashboard" className="btn primary">
                Ir a mi espacio
              </Link>
            ) : (
              <>
                <Link to="/login" className="btn primary">
                  Iniciar sesión
                </Link>
                <Link to="/register" className="btn secondary">
                  Registrarme
                </Link>
              </>
            )}
          </div>
        </div>

        <div className="card hero-card" aria-labelledby="panel-beneficios">
          <h2 id="panel-beneficios" className="hero-panel-title">
            Qué puedes hacer aquí
          </h2>
          <div className="feature-grid" role="list">
            <div className="feature-item" role="listitem">
              <div className="feature-icon" aria-hidden>
                📚
              </div>
              <div>
                <strong>Aprender a tu ritmo</strong>
                <span>Visualiza cursos disponibles y el avance en cada módulo desde el panel.</span>
              </div>
            </div>
            <div className="feature-item" role="listitem">
              <div className="feature-icon" aria-hidden>
                💬
              </div>
              <div>
                <strong>Debates por curso</strong>
                <span>Entra al foro de cada asignatura para coordinarte con otros estudiantes.</span>
              </div>
            </div>
            <div className="feature-item" role="listitem">
              <div className="feature-icon" aria-hidden>
                🎓
              </div>
              <div>
                <strong>Reconocimientos</strong>
                <span>Accede a tus certificados y mantén tu perfil al día.</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
