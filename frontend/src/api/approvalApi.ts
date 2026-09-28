import { apiFetch } from './apiClient'
import type { Approval, ApprovalQueueItem, ApprovalRole } from '../types/approval'

export type ApprovalDecisionRequest = {
  comments?: string
}

export async function getDealApprovals(dealId: number): Promise<Approval[]> {
  const response = await apiFetch(`/api/deals/${dealId}/approvals`)

  if (!response.ok) {
    throw new Error('Unable to load approval history.')
  }

  return response.json()
}

export async function getPendingApprovals(role: ApprovalRole): Promise<ApprovalQueueItem[]> {
  const response = await apiFetch(`/api/approvals/pending?role=${role}`)

  if (!response.ok) {
    const body = await response.json().catch(() => null)
    throw new Error(body?.message ?? 'Unable to load approval queue.')
  }

  return response.json()
}

async function getApprovalActionError(response: Response, fallback: string): Promise<Error> {
  const body = await response.json().catch(() => null)
  return new Error(body?.message ?? fallback)
}

async function postApprovalDecision(
  approvalId: number,
  path: string,
  request: ApprovalDecisionRequest,
  fallback: string,
): Promise<Approval> {
  const response = await apiFetch(`/api/approvals/${approvalId}/${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })

  if (!response.ok) {
    throw await getApprovalActionError(response, fallback)
  }

  return response.json()
}

export function approveApproval(
  approvalId: number,
  request: ApprovalDecisionRequest = {},
): Promise<Approval> {
  return postApprovalDecision(approvalId, 'approve', request, 'Unable to approve request.')
}

export function rejectApproval(
  approvalId: number,
  request: ApprovalDecisionRequest = {},
): Promise<Approval> {
  return postApprovalDecision(approvalId, 'reject', request, 'Unable to reject approval.')
}

export function requestApprovalChanges(
  approvalId: number,
  request: ApprovalDecisionRequest = {},
): Promise<Approval> {
  return postApprovalDecision(
    approvalId,
    'request-changes',
    request,
    'Unable to request changes.',
  )
}
