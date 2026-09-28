export type ExceptionType =
  | 'DISCOUNT'
  | 'MARGIN'

export type ExceptionStatus =
  | 'PENDING'
  | 'APPROVED'
  | 'REJECTED'
  | 'SUPERSEDED'

export type ExceptionQueueItem = {
  exceptionId: number
  dealId: number
  quoteNumber: string
  customerName: string
  dealValue: number
  discountPercentage: number
  marginPercentage: number
  dealStatus: string
  exceptionType: ExceptionType
  requestedValue: number
  standardValue: number
  justification: string
  exceptionStatus: ExceptionStatus
  createdAt?: string
}

export type DealException = {
  id: number
  dealId: number
  exceptionType: ExceptionType
  requestedValue: number
  standardValue: number
  justification: string
  status: ExceptionStatus
  createdBy?: number | null
  resolvedBy?: number | null
  createdAt?: string
  resolvedAt?: string | null
}

export type ExceptionRequirement = {
  exceptionType: ExceptionType
  requestedValue: number
  standardValue: number
  satisfied: boolean
  exceptionId?: number | null
  exceptionStatus?: ExceptionStatus | null
}
