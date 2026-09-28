export type DealStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'MANAGER_REVIEW'
  | 'FINANCE_REVIEW'
  | 'VP_REVIEW'
  | 'APPROVED'
  | 'REJECTED'
  | 'CHANGES_REQUESTED'
  | 'CANCELLED'

export type Deal = {
  id: number
  quoteNumber: string
  customerName: string
  dealValue: number
  discountPercentage: number
  marginPercentage: number
  status: DealStatus
  createdAt?: string
  updatedAt?: string
}
