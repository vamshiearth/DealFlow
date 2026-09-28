import type { DealStatus } from '../types/deal'

const currencyFormatter = new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD',
  maximumFractionDigits: 0,
})

export function formatCurrency(value: number): string {
  return currencyFormatter.format(value)
}

export function formatPercentage(value: number): string {
  return `${value}%`
}

export function formatDealStatus(status: DealStatus): string {
  return status
    .split('_')
    .map((word) => word.charAt(0) + word.slice(1).toLowerCase())
    .join(' ')
}

export function getDealStatusClass(status: DealStatus): string {
  return status.toLowerCase().replaceAll('_', '-')
}

export function formatRole(role: string): string {
  if (role === 'VP_SALES') {
    return 'VP Sales'
  }

  if (role === 'CPQ_ADMIN') {
    return 'CPQ Admin'
  }

  return role
    .split('_')
    .map((word) => word.charAt(0) + word.slice(1).toLowerCase())
    .join(' ')
}

export function formatStatus(status: string): string {
  return status
    .split('_')
    .map((word) => word.charAt(0) + word.slice(1).toLowerCase())
    .join(' ')
}

export function getStatusClass(status: string): string {
  return status.toLowerCase().replaceAll('_', '-')
}

export function formatDateTime(value?: string | null): string {
  if (!value) {
    return '—'
  }

  return new Intl.DateTimeFormat('en-US', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}
