import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'

import {
  approveApproval,
  getDealApprovals,
  rejectApproval,
  requestApprovalChanges,
} from '../api/approvalApi'
import { getDealAudit } from '../api/auditApi'
import {
  getDeal,
  resubmitDeal,
  submitDeal,
  updateDeal,
} from '../api/dealApi'
import {
  approveException,
  createException,
  getDealExceptions,
  getExceptionRequirements,
  rejectException,
} from '../api/exceptionApi'
import { useAuth } from '../auth/AuthContext'
import type { AuditEvent } from '../types/audit'
import type { Approval } from '../types/approval'
import type { Deal } from '../types/deal'
import type { DealException, ExceptionRequirement } from '../types/exception'
import {
  formatCurrency,
  formatDateTime,
  formatDealStatus,
  formatPercentage,
  formatRole,
  formatStatus,
  getDealStatusClass,
  getStatusClass,
} from '../utils/formatters'

type ApprovalAction = 'APPROVE' | 'REJECT' | 'REQUEST_CHANGES'

function getCurrentApprovalRole(status: Deal['status']): Approval['approverRole'] | null {
  switch (status) {
    case 'MANAGER_REVIEW':
      return 'SALES_MANAGER'
    case 'FINANCE_REVIEW':
      return 'FINANCE'
    case 'VP_REVIEW':
      return 'VP_SALES'
    default:
      return null
  }
}

function DealDetailPage() {
  const { dealId } = useParams()
  const numericDealId = Number(dealId)
  const { user } = useAuth()
  const [deal, setDeal] = useState<Deal | null>(null)
  const [approvals, setApprovals] = useState<Approval[]>([])
  const [exceptions, setExceptions] = useState<DealException[]>([])
  const [requirements, setRequirements] = useState<ExceptionRequirement[]>([])
  const [audit, setAudit] = useState<AuditEvent[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [actionSuccess, setActionSuccess] = useState<string | null>(null)
  const [actionLoading, setActionLoading] = useState(false)
  const [showEditForm, setShowEditForm] = useState(false)
  const [editCustomerName, setEditCustomerName] = useState('')
  const [editDealValue, setEditDealValue] = useState('')
  const [editDiscount, setEditDiscount] = useState('')
  const [editMargin, setEditMargin] = useState('')
  const [showExceptionForm, setShowExceptionForm] = useState(false)
  const [exceptionCreating, setExceptionCreating] = useState(false)
  const [exceptionError, setExceptionError] = useState<string | null>(null)
  const [exceptionJustification, setExceptionJustification] = useState('')
  const [selectedApproval, setSelectedApproval] = useState<Approval | null>(null)
  const [approvalAction, setApprovalAction] = useState<ApprovalAction | null>(null)
  const [approvalComments, setApprovalComments] = useState('')
  const [approvalDecisionLoading, setApprovalDecisionLoading] = useState(false)
  const [approvalDecisionError, setApprovalDecisionError] = useState<string | null>(null)

  async function loadWorkspace() {
    const [dealData, approvalData, exceptionData, requirementData, auditData] =
      await Promise.all([
        getDeal(numericDealId),
        getDealApprovals(numericDealId),
        getDealExceptions(numericDealId),
        getExceptionRequirements(numericDealId),
        getDealAudit(numericDealId),
      ])

    setDeal(dealData)
    setApprovals(approvalData)
    setExceptions(exceptionData)
    setRequirements(requirementData)
    setAudit(auditData)
  }

  useEffect(() => {
    if (!Number.isFinite(numericDealId) || numericDealId <= 0) {
      setError('Invalid Deal ID.')
      setLoading(false)
      return
    }

    async function loadDeal() {
      try {
        setLoading(true)
        setError(null)
        await loadWorkspace()
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unable to load deal.')
      } finally {
        setLoading(false)
      }
    }

    void loadDeal()
  }, [numericDealId])

  async function handleSubmitDeal() {
    try {
      setActionLoading(true)
      setActionError(null)
      setActionSuccess(null)
      await submitDeal(numericDealId)
      await loadWorkspace()
      setActionSuccess('Deal submitted for approval.')
    } catch (err) {
      setActionError(err instanceof Error ? err.message : 'Unable to submit deal.')
    } finally {
      setActionLoading(false)
    }
  }

  async function handleResubmitDeal() {
    try {
      setActionLoading(true)
      setActionError(null)
      setActionSuccess(null)
      await resubmitDeal(numericDealId)
      await loadWorkspace()
      setActionSuccess('Deal resubmitted for approval.')
    } catch (err) {
      setActionError(err instanceof Error ? err.message : 'Unable to resubmit deal.')
    } finally {
      setActionLoading(false)
    }
  }

  async function handleEditDeal(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    try {
      setActionLoading(true)
      setActionError(null)
      setActionSuccess(null)
      await updateDeal(numericDealId, {
        customerName: editCustomerName.trim(),
        dealValue: Number(editDealValue),
        discountPercentage: Number(editDiscount),
        marginPercentage: Number(editMargin),
      })
      setShowEditForm(false)
      await loadWorkspace()
      setActionSuccess('Deal updated successfully.')
    } catch (err) {
      setActionError(err instanceof Error ? err.message : 'Unable to update deal.')
    } finally {
      setActionLoading(false)
    }
  }

  async function handleCreateException() {
    if (!deal) {
      return
    }

    try {
      setExceptionCreating(true)
      setExceptionError(null)
      setActionSuccess(null)
      await createException(numericDealId, {
        exceptionType: 'DISCOUNT',
        requestedValue: deal.discountPercentage,
        standardValue: 20,
        justification: exceptionJustification.trim(),
      })
      setShowExceptionForm(false)
      setExceptionJustification('')
      await loadWorkspace()
      setActionSuccess('Exception created successfully.')
    } catch (err) {
      setExceptionError(err instanceof Error ? err.message : 'Unable to create exception.')
    } finally {
      setExceptionCreating(false)
    }
  }

  async function handleExceptionDecision(
    exceptionId: number,
    decision: 'approve' | 'reject',
  ) {
    try {
      setActionLoading(true)
      setActionError(null)
      setActionSuccess(null)
      if (decision === 'approve') {
        await approveException(exceptionId)
      } else {
        await rejectException(exceptionId)
      }
      await loadWorkspace()
      setActionSuccess(`Exception ${decision}d successfully.`)
    } catch (err) {
      setActionError(
        err instanceof Error
          ? err.message
          : `Unable to ${decision} exception.`,
      )
    } finally {
      setActionLoading(false)
    }
  }

  function openApprovalDecision(approval: Approval, action: ApprovalAction) {
    setSelectedApproval(approval)
    setApprovalAction(action)
    setApprovalComments('')
    setApprovalDecisionError(null)
  }

  async function handleApprovalDecision() {
    if (!selectedApproval || !approvalAction) {
      return
    }

    try {
      setApprovalDecisionLoading(true)
      setApprovalDecisionError(null)
      setActionSuccess(null)
      const request = { comments: approvalComments.trim() || undefined }

      if (approvalAction === 'APPROVE') {
        await approveApproval(selectedApproval.id, request)
      } else if (approvalAction === 'REJECT') {
        await rejectApproval(selectedApproval.id, request)
      } else {
        await requestApprovalChanges(selectedApproval.id, request)
      }

      setSelectedApproval(null)
      setApprovalAction(null)
      setApprovalComments('')
      await loadWorkspace()
      setActionSuccess('Approval completed successfully.')
    } catch (err) {
      setApprovalDecisionError(err instanceof Error ? err.message : 'Unable to process approval.')
    } finally {
      setApprovalDecisionLoading(false)
    }
  }

  if (loading) {
    return <div className="content-state">Loading deal...</div>
  }

  if (error || !deal) {
    return <div className="page-error">{error ?? 'Deal not found.'}</div>
  }

  const requirementsSatisfied = requirements.every((requirement) => requirement.satisfied)
  const canManageDeal = Boolean(
    user?.roles.includes('SALES_REP') || user?.roles.includes('CPQ_ADMIN'),
  )
  const canEdit = canManageDeal && (deal.status === 'DRAFT' || deal.status === 'CHANGES_REQUESTED')
  const canSubmit = canManageDeal && deal.status === 'DRAFT'
  const canResubmit = canManageDeal && deal.status === 'CHANGES_REQUESTED'
  const canCreateException = canManageDeal
  const canReviewExceptions = Boolean(
    user?.roles.includes('SALES_MANAGER') || user?.roles.includes('CPQ_ADMIN'),
  )
  const currentApprovalRole = getCurrentApprovalRole(deal.status)
  const currentApprovalCycle = approvals.length === 0
    ? null
    : Math.max(...approvals.map((approval) => approval.approvalCycle))
  const actionableApproval = approvals.find(
    (approval) => approval.approvalCycle === currentApprovalCycle
      && approval.status === 'PENDING'
      && approval.approverRole === currentApprovalRole,
  ) ?? null
  const isCpqAdmin = user?.roles.includes('CPQ_ADMIN') ?? false
  const canUserActOnApproval = (approval: Approval) => {
    if (approval.id !== actionableApproval?.id) {
      return false
    }

    return isCpqAdmin || (user?.roles.includes(approval.approverRole) ?? false)
  }

  return (
    <section>
      <div className="detail-back">
        <Link to="/deals">← Back to Deals</Link>
      </div>

      <div className="page-header">
        <div>
          <h2>{deal.quoteNumber}</h2>
          <p>{deal.customerName}</p>
        </div>
        <div className="detail-header-actions">
          <span className={`status-badge status-${getDealStatusClass(deal.status)}`}>
            {formatDealStatus(deal.status)}
          </span>
          {canEdit && <button type="button" className="secondary-button" onClick={() => {
            setEditCustomerName(deal.customerName)
            setEditDealValue(String(deal.dealValue))
            setEditDiscount(String(deal.discountPercentage))
            setEditMargin(String(deal.marginPercentage))
            setActionError(null)
            setShowEditForm(true)
          }}>Edit</button>}
          {canSubmit && <button type="button" className="primary-button" disabled={actionLoading} onClick={handleSubmitDeal}>Submit</button>}
          {canResubmit && <button type="button" className="primary-button" disabled={actionLoading} onClick={handleResubmitDeal}>Resubmit</button>}
        </div>
      </div>

      {actionError && <div className="page-error detail-action-error">{actionError}</div>}
      {actionSuccess && <div className="page-success">{actionSuccess}</div>}

      <div className="detail-grid">
        <article className="detail-card"><span>Deal Value</span><strong>{formatCurrency(deal.dealValue)}</strong></article>
        <article className="detail-card"><span>Discount</span><strong>{formatPercentage(deal.discountPercentage)}</strong></article>
        <article className="detail-card"><span>Margin</span><strong>{formatPercentage(deal.marginPercentage)}</strong></article>
        <article className="detail-card"><span>Exception Requirements</span><strong className={requirementsSatisfied ? 'requirement-ok' : 'requirement-warning'}>{requirementsSatisfied ? 'Satisfied' : 'Action required'}</strong></article>
      </div>

      <section className="detail-section">
        <h3>Approval Workflow <span className="section-count">{approvals.length}</span></h3>
        {approvals.length === 0 ? <div className="empty-state">No approvals created yet.</div> : (
          <div className="table-container"><table className="data-table"><thead><tr><th>Cycle</th><th>Role</th><th>Sequence</th><th>Status</th><th>Resolved By</th><th>Actions</th></tr></thead><tbody>
            {approvals.map((approval) => <tr key={approval.id}><td>{approval.approvalCycle}</td><td>{formatRole(approval.approverRole)}</td><td>{approval.sequence}</td><td><span className={`status-badge status-${getStatusClass(approval.status)}`}>{formatStatus(approval.status)}</span></td><td>{approval.resolvedBy ?? '—'}</td><td>{canUserActOnApproval(approval) ? <div className="row-actions"><button type="button" className="approve-button" disabled={approvalDecisionLoading} onClick={() => openApprovalDecision(approval, 'APPROVE')}>Approve</button><button type="button" className="secondary-button" disabled={approvalDecisionLoading} onClick={() => openApprovalDecision(approval, 'REQUEST_CHANGES')}>Request Changes</button><button type="button" className="danger-button" disabled={approvalDecisionLoading} onClick={() => openApprovalDecision(approval, 'REJECT')}>Reject</button></div> : '—'}</td></tr>)}
          </tbody></table></div>
        )}
      </section>

      {showEditForm && (
        <div className="modal-backdrop">
          <div className="modal-card" role="dialog" aria-modal="true" aria-labelledby="edit-deal-title">
            <div className="modal-header">
              <div>
                <h3 id="edit-deal-title">Edit Deal</h3>
                <p>Update the commercial terms before submission.</p>
              </div>
              <button type="button" className="icon-button" onClick={() => setShowEditForm(false)} aria-label="Close">×</button>
            </div>
            <form className="deal-form" onSubmit={handleEditDeal}>
              <div className="form-field">
                <label htmlFor="editQuoteNumber">Quote Number</label>
                <input id="editQuoteNumber" value={deal.quoteNumber} disabled />
              </div>
              <div className="form-field">
                <label htmlFor="editCustomer">Customer Name</label>
                <input id="editCustomer" value={editCustomerName} onChange={(event) => setEditCustomerName(event.target.value)} required />
              </div>
              <div className="form-grid">
                <div className="form-field"><label htmlFor="editValue">Deal Value</label><input id="editValue" type="number" min="0" step="0.01" value={editDealValue} onChange={(event) => setEditDealValue(event.target.value)} required /></div>
                <div className="form-field"><label htmlFor="editDiscount">Discount %</label><input id="editDiscount" type="number" min="0" max="100" step="0.01" value={editDiscount} onChange={(event) => setEditDiscount(event.target.value)} required /></div>
                <div className="form-field"><label htmlFor="editMargin">Margin %</label><input id="editMargin" type="number" min="0" max="100" step="0.01" value={editMargin} onChange={(event) => setEditMargin(event.target.value)} required /></div>
              </div>
              <div className="modal-actions">
                <button type="button" className="secondary-button" disabled={actionLoading} onClick={() => setShowEditForm(false)}>Cancel</button>
                <button type="submit" className="primary-button" disabled={actionLoading}>{actionLoading ? 'Saving...' : 'Save Changes'}</button>
              </div>
            </form>
          </div>
        </div>
      )}

      <section className="detail-section">
        <div className="detail-section-header">
          <h3>Exceptions <span className="section-count">{exceptions.length}</span></h3>
          {canCreateException && !requirementsSatisfied && (
            <button type="button" className="primary-button" onClick={() => {
              setExceptionError(null)
              setShowExceptionForm(true)
            }}>Create Exception</button>
          )}
        </div>
        {exceptions.length === 0 ? <div className="empty-state">No exceptions.</div> : (
          <div className="table-container"><table className="data-table"><thead><tr><th>Type</th><th>Requested</th><th>Standard</th><th>Status</th><th>Actions</th></tr></thead><tbody>
            {exceptions.map((exception) => <tr key={exception.id}><td>{formatStatus(exception.exceptionType)}</td><td>{formatPercentage(exception.requestedValue)}</td><td>{formatPercentage(exception.standardValue)}</td><td><span className={`status-badge status-${getStatusClass(exception.status)}`}>{formatStatus(exception.status)}</span></td><td>{exception.status === 'PENDING' && canReviewExceptions ? <div className="row-actions"><button type="button" className="approve-button" disabled={actionLoading} onClick={() => handleExceptionDecision(exception.id, 'approve')}>Approve</button><button type="button" className="danger-button" disabled={actionLoading} onClick={() => handleExceptionDecision(exception.id, 'reject')}>Reject</button></div> : '—'}</td></tr>)}
          </tbody></table></div>
        )}
      </section>

      {showExceptionForm && (
        <div className="modal-backdrop">
          <div className="modal-card" role="dialog" aria-modal="true" aria-labelledby="exception-title">
            <div className="modal-header">
              <div><h3 id="exception-title">Create Discount Exception</h3><p>Request approval for a discount above the standard limit.</p></div>
              <button type="button" className="icon-button" onClick={() => setShowExceptionForm(false)} aria-label="Close">×</button>
            </div>
            <div className="deal-form">
              <div className="detail-grid">
                <article className="detail-card"><span>Current Discount</span><strong>{formatPercentage(deal.discountPercentage)}</strong></article>
                <article className="detail-card"><span>Standard Limit</span><strong>20%</strong></article>
              </div>
              <div className="form-field">
                <label htmlFor="exceptionJustification">Justification</label>
                <textarea id="exceptionJustification" value={exceptionJustification} onChange={(event) => setExceptionJustification(event.target.value)} required rows={4} placeholder="Explain why this discount exception is needed." />
              </div>
              {exceptionError && <div className="page-error">{exceptionError}</div>}
              <div className="modal-actions">
                <button type="button" className="secondary-button" disabled={exceptionCreating} onClick={() => setShowExceptionForm(false)}>Cancel</button>
                <button type="button" className="primary-button" disabled={exceptionCreating} onClick={handleCreateException}>{exceptionCreating ? 'Creating...' : 'Create Exception'}</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {selectedApproval && approvalAction && (
        <div className="modal-backdrop">
          <div className="modal-card" role="dialog" aria-modal="true" aria-labelledby="approval-decision-title">
            <div className="modal-header">
              <div>
                <h3 id="approval-decision-title">
                  {approvalAction === 'APPROVE' ? 'Approve Deal' : approvalAction === 'REJECT' ? 'Reject Deal' : 'Request Changes'}
                </h3>
                <p>{selectedApproval.approverRole} approval · Cycle {selectedApproval.approvalCycle}</p>
              </div>
              <button type="button" className="icon-button" aria-label="Close" onClick={() => {
                setSelectedApproval(null)
                setApprovalAction(null)
              }}>×</button>
            </div>
            <div className="deal-form">
              <div className="form-field">
                <label htmlFor="approvalComments">Comments</label>
                <textarea
                  id="approvalComments"
                  value={approvalComments}
                  onChange={(event) => setApprovalComments(event.target.value)}
                  rows={4}
                  placeholder={approvalAction === 'REQUEST_CHANGES' ? 'Describe what needs to change...' : 'Optional review comments...'}
                />
              </div>
              {approvalDecisionError && <div className="page-error">{approvalDecisionError}</div>}
              <div className="modal-actions">
                <button type="button" className="secondary-button" disabled={approvalDecisionLoading} onClick={() => {
                  setSelectedApproval(null)
                  setApprovalAction(null)
                }}>Cancel</button>
                <button
                  type="button"
                  className={approvalAction === 'REJECT' ? 'danger-button' : approvalAction === 'APPROVE' ? 'approve-button' : 'primary-button'}
                  disabled={approvalDecisionLoading}
                  onClick={handleApprovalDecision}
                >
                  {approvalDecisionLoading ? 'Processing...' : approvalAction === 'APPROVE' ? 'Approve' : approvalAction === 'REJECT' ? 'Reject' : 'Request Changes'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      <section className="detail-section">
        <h3>Audit History <span className="section-count">{audit.length}</span></h3>
        {audit.length === 0 ? <div className="empty-state">No audit events.</div> : (
          <div className="audit-list">{audit.map((event) => <div className="audit-item" key={event.id}><div><strong>{formatStatus(event.eventType)}</strong><small>{formatStatus(event.entityType)}</small></div><div className="audit-meta"><span>{event.actorType === 'SYSTEM' ? 'System' : event.actorUserId ? `User ${event.actorUserId}` : 'User'}</span><span>{formatDateTime(event.createdAt)}</span></div></div>)}</div>
        )}
      </section>
    </section>
  )
}

export default DealDetailPage
