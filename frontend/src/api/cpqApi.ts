import { apiFetch } from './apiClient'
import type { CpqQuote, CpqQuoteDetail } from '../types/cpq'

export async function getCpqQuote(quoteNumber: string): Promise<CpqQuote> {
  const response = await apiFetch(
    `/api/cpq/quotes/${encodeURIComponent(quoteNumber)}`,
  )

  if (!response.ok) {
    const body = await response.json().catch(() => null)
    throw new Error(body?.message ?? 'Unable to load CPQ quote.')
  }

  return response.json()
}

export async function getCpqQuoteDetails(quoteNumber: string): Promise<CpqQuoteDetail> {
  const response = await apiFetch(
    `/api/cpq/quotes/${encodeURIComponent(quoteNumber)}/details`,
  )

  if (!response.ok) {
    const body = await response.json().catch(() => null)
    throw new Error(body?.message ?? 'Unable to load CPQ quote details.')
  }

  return response.json()
}
