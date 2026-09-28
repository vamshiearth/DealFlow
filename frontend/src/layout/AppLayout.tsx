import { NavLink, Outlet, useNavigate } from 'react-router-dom'

import { useAuth } from '../auth/AuthContext'
import { canSeeNavigationItem, navigationItems } from '../config/navigation'
import { formatRole } from '../utils/formatters'

function AppLayout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="app-layout">
      <aside className="sidebar">
        <div className="brand">
          <h1>DealFlow</h1>
          <span>CPQ Deal Desk</span>
        </div>

        <nav className="sidebar-nav" aria-label="Main navigation">
          {navigationItems
            .filter((item) => canSeeNavigationItem(user?.roles ?? [], item))
            .map((item) => (
              <NavLink key={item.path} to={item.path}>
                {item.label}
              </NavLink>
            ))}
        </nav>

        <div className="sidebar-footer">
          <button type="button" onClick={handleLogout}>
            Sign out
          </button>
        </div>
      </aside>

      <div className="app-main">
        <header className="topbar">
          <div>
            <strong>
              {user?.firstName} {user?.lastName}
            </strong>
            <span>{user?.roles.map(formatRole).join(', ')}</span>
          </div>
        </header>

        <main className="page-content">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

export default AppLayout
