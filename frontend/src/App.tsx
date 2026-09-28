import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
} from 'react-router-dom'

import { AuthProvider } from './auth/AuthContext'
import ProtectedRoute from './auth/ProtectedRoute'
import AppLayout from './layout/AppLayout'
import DashboardPage from './pages/DashboardPage'
import DealsPage from './pages/DealsPage'
import DealDetailPage from './pages/DealDetailPage'
import ApprovalsPage from './pages/ApprovalsPage'
import ExceptionsPage from './pages/ExceptionsPage'
import CpqPage from './pages/CpqPage'
import LoginPage from './pages/LoginPage'

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            element={
              <ProtectedRoute>
                <AppLayout />
              </ProtectedRoute>
            }
          >
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/deals" element={<DealsPage />} />
            <Route path="/deals/:dealId" element={<DealDetailPage />} />
            <Route path="/approvals" element={<ApprovalsPage />} />
            <Route path="/exceptions" element={<ExceptionsPage />} />
            <Route path="/cpq" element={<CpqPage />} />
          </Route>
          <Route
            path="/"
            element={
              <Navigate to="/dashboard" replace />
            }
          />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}

export default App