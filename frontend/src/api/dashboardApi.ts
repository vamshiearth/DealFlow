import { apiFetch } from './apiClient'

import type { UserRole } from '../auth/authTypes'
import type { ApprovalQueueItem, ApprovalRole } from '../types/approval'
import type { Deal } from '../types/deal'
import type { ExceptionQueueItem } from '../types/exception'

export type DashboardData = {
  deals: Deal[]
  pendingApprovals: ApprovalQueueItem[]
  pendingExceptions: ExceptionQueueItem[]
}

async function getDeals(): Promise<Deal[]> {
  const response = await apiFetch('/api/deals')

  if (!response.ok) {
    throw new Error('Unable to load deals.')
  }

  return response.json()
}

async function getPendingExceptions(): Promise<ExceptionQueueItem[]> {
  const response = await apiFetch('/api/exceptions/pending')

  if (!response.ok) {
    throw new Error('Unable to load pending exceptions.')
  }

  return response.json()
}

async function getApprovalQueue(
  role: ApprovalRole,
): Promise<ApprovalQueueItem[]> {
  const response = await apiFetch(
    `/api/approvals/pending?role=${role}`,
  )

  if (!response.ok) {
    throw new Error(`Unable to load ${role} approvals.`)
  }

  return response.json()
}

async function getPendingApprovals(
  roles: UserRole[],
): Promise<ApprovalQueueItem[]> {
  if (roles.includes('CPQ_ADMIN')) {
    const [manager, finance, vp] = await Promise.all([
      getApprovalQueue('SALES_MANAGER'),
      getApprovalQueue('FINANCE'),
      getApprovalQueue('VP_SALES'),
    ])

    return [...manager, ...finance, ...vp]
  }

  if (roles.includes('SALES_MANAGER')) {
    return getApprovalQueue('SALES_MANAGER')
  }

  if (roles.includes('FINANCE')) {
    return getApprovalQueue('FINANCE')
  }

  if (roles.includes('VP_SALES')) {
    return getApprovalQueue('VP_SALES')
  }

  return []
}

export async function getDashboardData(
  roles: UserRole[],
): Promise<DashboardData> {
  const [deals, pendingApprovals, pendingExceptions] = await Promise.all([
    getDeals(),
    getPendingApprovals(roles),
    getPendingExceptions(),
  ])

  return { deals, pendingApprovals, pendingExceptions }
}
