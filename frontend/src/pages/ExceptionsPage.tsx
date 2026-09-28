import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import {
  approveException,
  getPendingExceptions,
  rejectException,
} from '../api/exceptionApi'
import { useAuth } from '../auth/AuthContext'
import type { ExceptionQueueItem } from '../types/exception'
import {
  formatCurrency,
  formatPercentage,
  formatStatus,
} from '../utils/formatters'

type ExceptionAction = 'APPROVE' | 'REJECT'

function ExceptionsPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [exceptions, setExceptions] = useState<ExceptionQueueItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedException, setSelectedException] = useState<ExceptionQueueItem | null>(null)
  const [exceptionAction, setExceptionAction] = useState<ExceptionAction | null>(null)
  const [decisionLoading, setDecisionLoading] = useState(false)
  const [decisionError, setDecisionError] = useState<string | null>(null)
  const [decisionSuccess, setDecisionSuccess] = useState<string | null>(null)
  const canReviewExceptions = Boolean(
    user?.roles.includes('SALES_MANAGER') || user?.roles.includes('CPQ_ADMIN'),
  )

  async function loadQueue() {
    if (!user || !canReviewExceptions) {
      setExceptions([])
      setLoading(false)
      return
    }

    try {
      setLoading(true)
      setError(null)
      setExceptions(await getPendingExceptions())
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to load exceptions.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadQueue()
  }, [user, canReviewExceptions])

  function openDecision(exception: ExceptionQueueItem, action: ExceptionAction) {
    setSelectedException(exception)
    setExceptionAction(action)
    setDecisionError(null)
    setDecisionSuccess(null)
  }

  async function handleDecision() {
    if (!selectedException || !exceptionAction) {
      return
    }

    try {
      setDecisionLoading(true)
      setDecisionError(null)
      setDecisionSuccess(null)

      if (exceptionAction === 'APPROVE') {
        await approveException(selectedException.exceptionId)
        setDecisionSuccess('Exception approved successfully.')
      } else {
        await rejectException(selectedException.exceptionId)
        setDecisionSuccess('Exception rejected successfully.')
      }

      setSelectedException(null)
      setExceptionAction(null)
      await loadQueue()
    } catch (err) {
      setDecisionError(err instanceof Error ? err.message : 'Unable to process exception.')
    } finally {
      setDecisionLoading(false)
    }
  }

  return (
    <section>
      <div className="page-header">
        <div>
          <h2>Exceptions</h2>
          <p>Review commercial exceptions requiring approval.</p>
        </div>
      </div>

      {loading && <div className="content-state">Loading exceptions...</div>}
      {error && <div className="page-error">{error}</div>}
      {decisionSuccess && <div className="page-success">{decisionSuccess}</div>}
      {!loading && !error && !canReviewExceptions && (
        <div className="empty-state">Your role does not have an exception review queue.</div>
      )}
      {!loading && !error && canReviewExceptions && exceptions.length === 0 && (
        <div className="empty-state">No exceptions are currently waiting for review.</div>
      )}
      {!loading && !error && exceptions.length > 0 && (
        <div className="table-card">
          <div className="table-container">
            <table className="data-table">
              <thead><tr><th>Quote</th><th>Customer</th><th>Value</th><th>Type</th><th>Requested</th><th>Standard</th><th>Status</th><th>Actions</th></tr></thead>
              <tbody>
                {exceptions.map((exception) => (
                  <tr key={exception.exceptionId}>
                    <td><strong>{exception.quoteNumber}</strong></td>
                    <td>{exception.customerName}</td>
                    <td>{formatCurrency(exception.dealValue)}</td>
                    <td>{formatStatus(exception.exceptionType)}</td>
                    <td>{formatPercentage(exception.requestedValue)}</td>
                    <td>{formatPercentage(exception.standardValue)}</td>
                    <td><span className="status-badge status-pending">{formatStatus(exception.exceptionStatus)}</span></td>
                    <td><div className="row-actions"><button type="button" className="approve-button" disabled={decisionLoading} onClick={() => openDecision(exception, 'APPROVE')}>Approve</button><button type="button" className="danger-button" disabled={decisionLoading} onClick={() => openDecision(exception, 'REJECT')}>Reject</button><button type="button" className="secondary-button" disabled={decisionLoading} onClick={() => navigate(`/deals/${exception.dealId}`)}>Review</button></div></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {selectedException && exceptionAction && (
        <div className="modal-backdrop">
          <div className="modal-card" role="dialog" aria-modal="true" aria-labelledby="exception-decision-title">
            <div className="modal-header">
              <div>
                <h3 id="exception-decision-title">{exceptionAction === 'APPROVE' ? 'Approve Exception' : 'Reject Exception'}</h3>
                <p>{formatStatus(selectedException.exceptionType)} exception</p>
              </div>
              <button type="button" className="icon-button" disabled={decisionLoading} onClick={() => {
                setSelectedException(null)
                setExceptionAction(null)
                setDecisionError(null)
              }} aria-label="Close">×</button>
            </div>
            <div className="deal-form">
              <div className="exception-decision-summary">
                <div><span>Requested</span><strong>{formatPercentage(selectedException.requestedValue)}</strong></div>
                <div><span>Standard</span><strong>{formatPercentage(selectedException.standardValue)}</strong></div>
              </div>
              {decisionError && <div className="page-error">{decisionError}</div>}
              <div className="modal-actions">
                <button type="button" className="secondary-button" disabled={decisionLoading} onClick={() => {
                  setSelectedException(null)
                  setExceptionAction(null)
                }}>Cancel</button>
                <button type="button" className={exceptionAction === 'APPROVE' ? 'approve-button' : 'danger-button'} disabled={decisionLoading} onClick={handleDecision}>{decisionLoading ? 'Processing...' : exceptionAction === 'APPROVE' ? 'Approve' : 'Reject'}</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </section>
  )
}

export default ExceptionsPage
