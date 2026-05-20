import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RootErrorBoundary } from './components/RootErrorBoundary'
import { AuthProvider } from './context/AuthContext'
import './index.css'
import './aula-theme.css'
import App from './App.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <RootErrorBoundary>
      <AuthProvider>
        <App />
      </AuthProvider>
    </RootErrorBoundary>
  </StrictMode>,
)
