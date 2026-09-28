export type ApprovalRole =
  | 'SALES_MANAGER'
  | 'FINANCE'
  | 'VP_SALES'

export type ApprovalStatus =
  | 'PENDING'
  | 'APPROVED'
  | 'REJECTED'
  | 'CHANGES_REQUESTED'
  | 'SUPERSEDED'

export type ApprovalQueueItem = {
  approvalId: number
  dealId: number
  quoteNumber: string
  customerName: string
  dealValue: number
  discountPercentage: number
  marginPercentage: number
  dealStatus: string
  approverRole: ApprovalRole
  approvalStatus: ApprovalStatus
  sequence: number
  approvalCycle: number
  requestedAt?: string
}

export type Approval = {
  id: number
  dealId: number
  approverRole: ApprovalRole
  status: ApprovalStatus
  sequence: number
  approvalCycle: number
  resolvedBy?: number | null
  requestedAt?: string
  resolvedAt?: string | null
  comments?: string | null
}
