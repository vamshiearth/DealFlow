import { apiFetch } from './apiClient'
import type { AuditEvent } from '../types/audit'

export async function getDealAudit(dealId: number): Promise<AuditEvent[]> {
  const response = await apiFetch(`/api/deals/${dealId}/audit`)

  if (!response.ok) {
    throw new Error('Unable to load audit history.')
  }

  return response.json()
}
