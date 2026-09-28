import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import {
  approveApproval,
  getPendingApprovals,
  rejectApproval,
  requestApprovalChanges,
} from '../api/approvalApi'
import { useAuth } from '../auth/AuthContext'
import type { ApprovalQueueItem, ApprovalRole } from '../types/approval'
import {
  formatCurrency,
  formatRole,
  formatStatus,
} from '../utils/formatters'

type ApprovalAction = 'APPROVE' | 'REJECT' | 'REQUEST_CHANGES'

function ApprovalsPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [approvals, setApprovals] = useState<ApprovalQueueItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedApproval, setSelectedApproval] = useState<ApprovalQueueItem | null>(null)
  const [approvalAction, setApprovalAction] = useState<ApprovalAction | null>(null)
  const [comments, setComments] = useState('')
  const [decisionLoading, setDecisionLoading] = useState(false)
  const [decisionError, setDecisionError] = useState<string | null>(null)
  const [decisionSuccess, setDecisionSuccess] = useState<string | null>(null)

  async function loadQueue() {
    if (!user) {
      return
    }

    const roles: ApprovalRole[] = user.roles.includes('CPQ_ADMIN')
      ? ['SALES_MANAGER', 'FINANCE', 'VP_SALES']
      : user.roles.includes('SALES_MANAGER')
        ? ['SALES_MANAGER']
        : user.roles.includes('FINANCE')
          ? ['FINANCE']
          : user.roles.includes('VP_SALES')
            ? ['VP_SALES']
            : []

    if (roles.length === 0) {
      setApprovals([])
      setLoading(false)
      return
    }

    try {
      setLoading(true)
      setError(null)
      const queueResults = await Promise.all(roles.map((role) => getPendingApprovals(role)))
      setApprovals(queueResults.flat().sort((left, right) => right.approvalId - left.approvalId))
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to load approvals.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadQueue()
  }, [user])

  function openDecision(approval: ApprovalQueueItem, action: ApprovalAction) {
    setSelectedApproval(approval)
    setApprovalAction(action)
    setComments('')
    setDecisionError(null)
    setDecisionSuccess(null)
  }

  async function handleDecision() {
    if (!selectedApproval || !approvalAction) {
      return
    }

    try {
      setDecisionLoading(true)
      setDecisionError(null)
      setDecisionSuccess(null)
      const request = { comments: comments.trim() || undefined }

      if (approvalAction === 'APPROVE') {
        await approveApproval(selectedApproval.approvalId, request)
        setDecisionSuccess('Approval completed successfully.')
      } else if (approvalAction === 'REJECT') {
        await rejectApproval(selectedApproval.approvalId, request)
        setDecisionSuccess('Deal rejected successfully.')
      } else {
        await requestApprovalChanges(selectedApproval.approvalId, request)
        setDecisionSuccess('Changes requested successfully.')
      }

      setSelectedApproval(null)
      setApprovalAction(null)
      setComments('')
      await loadQueue()
    } catch (err) {
      setDecisionError(err instanceof Error ? err.message : 'Unable to process approval.')
    } finally {
      setDecisionLoading(false)
    }
  }

  const hasApprovalRole = user?.roles.some((role) =>
    ['SALES_MANAGER', 'FINANCE', 'VP_SALES', 'CPQ_ADMIN'].includes(role),
  ) ?? false

  return (
    <section>
      <div className="page-header">
        <div>
          <h2>Approvals</h2>
          <p>Review Deals currently waiting for approval.</p>
        </div>
      </div>

      {loading && <div className="content-state">Loading approvals...</div>}
      {error && <div className="page-error">{error}</div>}
      {decisionSuccess && <div className="page-success">{decisionSuccess}</div>}
      {!loading && !error && !hasApprovalRole && (
        <div className="empty-state">Your role does not have an approval queue.</div>
      )}
      {!loading && !error && hasApprovalRole && approvals.length === 0 && (
        <div className="empty-state">No approvals are currently waiting for your review.</div>
      )}
      {!loading && !error && approvals.length > 0 && (
        <div className="table-card">
          <div className="table-container">
            <table className="data-table">
              <thead><tr><th>Quote</th><th>Customer</th><th>Value</th><th>Role</th><th>Cycle</th><th>Status</th><th>Actions</th></tr></thead>
              <tbody>
                {approvals.map((approval) => (
                  <tr key={approval.approvalId}>
                    <td><strong>{approval.quoteNumber}</strong></td>
                    <td>{approval.customerName}</td>
                    <td>{formatCurrency(approval.dealValue)}</td>
                    <td>{formatRole(approval.approverRole)}</td>
                    <td>{approval.approvalCycle}</td>
                    <td><span className="status-badge status-pending">{formatStatus(approval.approvalStatus)}</span></td>
                    <td><div className="row-actions"><button type="button" className="approve-button" disabled={decisionLoading} onClick={() => openDecision(approval, 'APPROVE')}>Approve</button><button type="button" className="secondary-button" disabled={decisionLoading} onClick={() => openDecision(approval, 'REQUEST_CHANGES')}>Request Changes</button><button type="button" className="danger-button" disabled={decisionLoading} onClick={() => openDecision(approval, 'REJECT')}>Reject</button><button type="button" className="secondary-button" disabled={decisionLoading} onClick={() => navigate(`/deals/${approval.dealId}`)}>Review</button></div></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {selectedApproval && approvalAction && (
        <div className="modal-backdrop">
          <div className="modal-card" role="dialog" aria-modal="true" aria-labelledby="queue-decision-title">
            <div className="modal-header">
              <div>
                <h3 id="queue-decision-title">{approvalAction === 'APPROVE' ? 'Approve Deal' : approvalAction === 'REJECT' ? 'Reject Deal' : 'Request Changes'}</h3>
                <p>{formatRole(selectedApproval.approverRole)} · Cycle {selectedApproval.approvalCycle}</p>
              </div>
              <button type="button" className="icon-button" disabled={decisionLoading} onClick={() => {
                setSelectedApproval(null)
                setApprovalAction(null)
                setDecisionError(null)
              }} aria-label="Close">×</button>
            </div>
            <div className="deal-form">
              <div className="form-field">
                <label htmlFor="queueComments">Comments</label>
                <textarea id="queueComments" value={comments} onChange={(event) => setComments(event.target.value)} rows={4} placeholder={approvalAction === 'REQUEST_CHANGES' ? 'Describe what needs to change...' : 'Optional review comments...'} />
              </div>
              {decisionError && <div className="page-error">{decisionError}</div>}
              <div className="modal-actions">
                <button type="button" className="secondary-button" disabled={decisionLoading} onClick={() => {
                  setSelectedApproval(null)
                  setApprovalAction(null)
                }}>Cancel</button>
                <button type="button" className={approvalAction === 'APPROVE' ? 'approve-button' : approvalAction === 'REJECT' ? 'danger-button' : 'primary-button'} disabled={decisionLoading} onClick={handleDecision}>{decisionLoading ? 'Processing...' : approvalAction === 'APPROVE' ? 'Approve' : approvalAction === 'REJECT' ? 'Reject' : 'Request Changes'}</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </section>
  )
}

export default ApprovalsPage
