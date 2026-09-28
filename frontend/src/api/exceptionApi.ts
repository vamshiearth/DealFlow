import { apiFetch } from './apiClient'
import type {
  DealException,
  ExceptionQueueItem,
  ExceptionType,
  ExceptionRequirement,
} from '../types/exception'

export type CreateExceptionRequest = {
  exceptionType: ExceptionType
  requestedValue: number
  standardValue: number
  justification: string
}

export async function getDealExceptions(dealId: number): Promise<DealException[]> {
  const response = await apiFetch(`/api/deals/${dealId}/exceptions`)

  if (!response.ok) {
    throw new Error('Unable to load deal exceptions.')
  }

  return response.json()
}

export async function getPendingExceptions(): Promise<ExceptionQueueItem[]> {
  const response = await apiFetch('/api/exceptions/pending')

  if (!response.ok) {
    const body = await response.json().catch(() => null)
    throw new Error(body?.message ?? 'Unable to load exception queue.')
  }

  return response.json()
}

export async function getExceptionRequirements(
  dealId: number,
): Promise<ExceptionRequirement[]> {
  const response = await apiFetch(`/api/deals/${dealId}/exceptions/requirements`)

  if (!response.ok) {
    throw new Error('Unable to load exception requirements.')
  }

  return response.json()
}

async function getExceptionActionError(response: Response, fallback: string): Promise<Error> {
  const body = await response.json().catch(() => null)
  return new Error(body?.message ?? fallback)
}

export async function createException(
  dealId: number,
  request: CreateExceptionRequest,
): Promise<DealException> {
  const response = await apiFetch(`/api/deals/${dealId}/exceptions`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })

  if (!response.ok) {
    throw await getExceptionActionError(response, 'Unable to create exception.')
  }

  return response.json()
}

export async function approveException(exceptionId: number): Promise<DealException> {
  const response = await apiFetch(`/api/exceptions/${exceptionId}/approve`, { method: 'POST' })

  if (!response.ok) {
    throw await getExceptionActionError(response, 'Unable to approve exception.')
  }

  return response.json()
}

export async function rejectException(exceptionId: number): Promise<DealException> {
  const response = await apiFetch(`/api/exceptions/${exceptionId}/reject`, { method: 'POST' })

  if (!response.ok) {
    throw await getExceptionActionError(response, 'Unable to reject exception.')
  }

  return response.json()
}
