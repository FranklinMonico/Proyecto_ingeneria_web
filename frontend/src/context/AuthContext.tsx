import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { decodeJwtSubject } from '../lib/jwt'
import {
  apiRequest,
  clearStoredTokens,
  getStoredAccessToken,
  setStoredTokens,
} from '../lib/api'

type AuthContextValue = {
  accessToken: string | null
  email: string | null
  isAuthenticated: boolean
  login: (email: string, password: string) => Promise<void>
  logout: () => void
  refreshSession: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

function readEmailFromStorage(): string | null {
  const t = getStoredAccessToken()
  if (!t) return null
  return decodeJwtSubject(t)
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [accessToken, setAccessToken] = useState<string | null>(() => getStoredAccessToken())
  const [email, setEmail] = useState<string | null>(() => readEmailFromStorage())

  const refreshSession = useCallback(() => {
    setAccessToken(getStoredAccessToken())
    setEmail(readEmailFromStorage())
  }, [])

  const login = useCallback(async (userEmail: string, password: string) => {
    const res = await apiRequest<{ accessToken: string; refreshToken: string }>(
      '/api/auth/login',
      { method: 'POST', body: { email: userEmail, password }, skipAuth: true },
    )
    const data = res.data
    if (!res.success || !data?.accessToken || !data?.refreshToken) {
      throw new Error(res.message || 'Error al iniciar sesión')
    }
    setStoredTokens(data.accessToken, data.refreshToken)
    setAccessToken(data.accessToken)
    setEmail(decodeJwtSubject(data.accessToken))
  }, [])

  const logout = useCallback(() => {
    clearStoredTokens()
    setAccessToken(null)
    setEmail(null)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      accessToken,
      email,
      isAuthenticated: Boolean(accessToken),
      login,
      logout,
      refreshSession,
    }),
    [accessToken, email, login, logout, refreshSession],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth debe usarse dentro de AuthProvider')
  return ctx
}
