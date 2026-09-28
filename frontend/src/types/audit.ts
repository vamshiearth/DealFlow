export type AuditEvent = {
  id: number
  dealId: number
  eventType: string
  entityType: string
  entityId?: number | null
  actorType: 'USER' | 'SYSTEM'
  actorUserId?: number | null
  oldStatus?: string | null
  newStatus?: string | null
  description?: string | null
  createdAt?: string
}
