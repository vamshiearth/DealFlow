export type UserRole =
  | 'SALES_REP'
  | 'SALES_MANAGER'
  | 'FINANCE'
  | 'VP_SALES'
  | 'CPQ_ADMIN'

export type CurrentUser = {
  id: number
  firstName: string
  lastName: string
  email: string
  roles: UserRole[]
}

export type LoginResponse = {
  accessToken: string
  tokenType?: string
  expiresIn?: number
  userId?: number
  email?: string
  roles?: UserRole[]
}
