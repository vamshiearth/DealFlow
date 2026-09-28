import {
  createContext,
  useContext,
  useEffect,
  useState,
} from 'react'
import type { ReactNode } from 'react'

import type { CurrentUser, LoginResponse } from './authTypes'
import { clearToken, getToken, setToken } from './tokenStorage'
import { apiFetch } from '../api/apiClient'

type AuthContextValue = {
  user: CurrentUser | null
  loading: boolean
  isAuthenticated: boolean
  login: (email: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

type AuthProviderProps = {
  children: ReactNode
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<CurrentUser | null>(null)
  const [loading, setLoading] = useState(true)

  async function loadCurrentUser() {
    const response = await apiFetch('/api/auth/me')

    if (!response.ok) {
      throw new Error('Unable to load current user.')
    }

    const data: CurrentUser = await response.json()
    setUser(data)
  }

  useEffect(() => {
    async function restoreSession() {
      const token = getToken()

      if (!token) {
        setLoading(false)
        return
      }

      try {
        await loadCurrentUser()
      } catch {
        clearToken()
        setUser(null)
      } finally {
        setLoading(false)
      }
    }

    restoreSession()
  }, [])

  async function login(email: string, password: string) {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    })

    if (!response.ok) {
      const body = await response.json().catch(() => null)
      throw new Error(body?.message ?? 'Invalid email or password.')
    }

    const data: LoginResponse = await response.json()

    if (!data.accessToken) {
      throw new Error('Login response did not contain a token.')
    }

    setToken(data.accessToken)

    try {
      await loadCurrentUser()
    } catch (error) {
      clearToken()
      setUser(null)
      throw error
    }
  }

  function logout() {
    clearToken()
    setUser(null)
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        isAuthenticated: user !== null,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)

  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider.')
  }

  return context
}
