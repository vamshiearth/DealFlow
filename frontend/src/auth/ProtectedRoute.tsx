import { Navigate } from 'react-router-dom'
import type { ReactNode } from 'react'

import { useAuth } from './AuthContext'

type ProtectedRouteProps = {
  children: ReactNode
}

function ProtectedRoute({ children }: ProtectedRouteProps) {
  const { loading, isAuthenticated } = useAuth()

  if (loading) {
    return (
      <main className="page-center">
        <p>Loading DealFlow...</p>
      </main>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  return children
}

export default ProtectedRoute
