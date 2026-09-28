import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'

import { getDashboardData } from '../api/dashboardApi'
import type { DashboardData } from '../api/dashboardApi'
import { useAuth } from '../auth/AuthContext'
import {
  formatCurrency,
  formatDealStatus,
  formatPercentage,
  getDealStatusClass,
} from '../utils/formatters'

function DashboardPage() {
  const { user } = useAuth()
  const [data, setData] = useState<DashboardData | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!user) {
      return
    }

    async function loadDashboard() {
      try {
        setLoading(true)
        setError(null)
        setData(await getDashboardData(user.roles))
      } catch (err) {
        setError(
          err instanceof Error ? err.message : 'Unable to load dashboard.',
        )
      } finally {
        setLoading(false)
      }
    }

    void loadDashboard()
  }, [user])

  const approvedDeals = useMemo(
    () => data?.deals.filter((deal) => deal.status === 'APPROVED').length ?? 0,
    [data],
  )

  const recentDeals = useMemo(() => {
    if (!data) {
      return []
    }

    return [...data.deals]
      .sort((a, b) => {
        const aTime = a.updatedAt ?? a.createdAt
        const bTime = b.updatedAt ?? b.createdAt

        if (aTime && bTime) {
          return new Date(bTime).getTime() - new Date(aTime).getTime()
        }

        return b.id - a.id
      })
      .slice(0, 5)
  }, [data])

  if (loading) {
    return (
      <section>
        <h2>Dashboard</h2>
        <p>Loading dashboard...</p>
      </section>
    )
  }

  if (error) {
    return (
      <section>
        <h2>Dashboard</h2>
        <div className="page-error">{error}</div>
      </section>
    )
  }

  return (
    <section>
      <div className="page-header">
        <div>
          <h2>Dashboard</h2>
          <p>Overview of DealFlow activity.</p>
        </div>
      </div>

      <div className="metric-grid">
        <article className="metric-card">
          <span>Total Deals</span>
          <strong>{data?.deals.length ?? 0}</strong>
        </article>
        <article className="metric-card">
          <span>Approved Deals</span>
          <strong>{approvedDeals}</strong>
        </article>
        <article className="metric-card">
          <span>Pending Approvals</span>
          <strong>{data?.pendingApprovals.length ?? 0}</strong>
        </article>
        <article className="metric-card">
          <span>Pending Exceptions</span>
          <strong>{data?.pendingExceptions.length ?? 0}</strong>
        </article>
      </div>

      <div className="dashboard-section">
        <div className="section-header">
          <div>
            <h3>Recent Deals</h3>
            <p>Latest DealFlow activity.</p>
          </div>
          <Link to="/deals" className="section-link">
            View all deals
          </Link>
        </div>

        {recentDeals.length === 0 ? (
          <div className="empty-state">No deals available.</div>
        ) : (
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Quote</th>
                  <th>Customer</th>
                  <th>Value</th>
                  <th>Discount</th>
                  <th>Margin</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {recentDeals.map((deal) => (
                  <tr key={deal.id}>
                    <td><strong>{deal.quoteNumber}</strong></td>
                    <td>{deal.customerName}</td>
                    <td>{formatCurrency(deal.dealValue)}</td>
                    <td>{formatPercentage(deal.discountPercentage)}</td>
                    <td>{formatPercentage(deal.marginPercentage)}</td>
                    <td>
                      <span className={`status-badge status-${getDealStatusClass(deal.status)}`}>
                        {formatDealStatus(deal.status)}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  )
}

export default DashboardPage
