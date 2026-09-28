import { apiFetch } from './apiClient'
import type { Deal } from '../types/deal'

export type CreateDealRequest = {
  quoteNumber: string
  customerName: string
  dealValue: number
  discountPercentage: number
  marginPercentage: number
}

export type UpdateDealRequest = {
  customerName: string
  dealValue: number
  discountPercentage: number
  marginPercentage: number
}

export async function getDeals(): Promise<Deal[]> {
  const response = await apiFetch('/api/deals')

  if (!response.ok) {
    throw new Error('Unable to load deals.')
  }

  return response.json()
}

export async function getDeal(dealId: number): Promise<Deal> {
  const response = await apiFetch(`/api/deals/${dealId}`)

  if (!response.ok) {
    const body = await response.json().catch(() => null)
    throw new Error(body?.message ?? 'Unable to load deal.')
  }

  return response.json()
}

async function getActionError(response: Response, fallback: string): Promise<Error> {
  const body = await response.json().catch(() => null)
  return new Error(body?.message ?? fallback)
}

export async function updateDeal(
  dealId: number,
  request: UpdateDealRequest,
): Promise<Deal> {
  const response = await apiFetch(`/api/deals/${dealId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })

  if (!response.ok) {
    throw await getActionError(response, 'Unable to update deal.')
  }

  return response.json()
}

export async function submitDeal(dealId: number): Promise<Deal> {
  const response = await apiFetch(`/api/deals/${dealId}/submit`, { method: 'POST' })

  if (!response.ok) {
    throw await getActionError(response, 'Unable to submit deal.')
  }

  return response.json()
}

export async function resubmitDeal(dealId: number): Promise<Deal> {
  const response = await apiFetch(`/api/deals/${dealId}/resubmit`, { method: 'POST' })

  if (!response.ok) {
    throw await getActionError(response, 'Unable to resubmit deal.')
  }

  return response.json()
}

export async function createDeal(
  request: CreateDealRequest,
): Promise<Deal> {
  const response = await apiFetch('/api/deals', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  })

  if (!response.ok) {
    const body = await response.json().catch(() => null)
    throw new Error(body?.message ?? 'Unable to create deal.')
  }

  return response.json()
}

export async function importDealFromCpq(quoteNumber: string): Promise<Deal> {
  const response = await apiFetch(
    `/api/deals/import-from-cpq/${encodeURIComponent(quoteNumber)}`,
    { method: 'POST' },
  )

  if (!response.ok) {
    const body = await response.json().catch(() => null)
    throw new Error(body?.message ?? 'Unable to import CPQ quote.')
  }

  return response.json()
}
