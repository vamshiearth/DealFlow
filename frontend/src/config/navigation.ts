import type { UserRole } from '../auth/authTypes'

export type NavigationItem = {
  label: string
  path: string
  roles?: UserRole[]
}

export const navigationItems: NavigationItem[] = [
  { label: 'Dashboard', path: '/dashboard' },
  { label: 'Deals', path: '/deals' },
  {
    label: 'Approvals',
    path: '/approvals',
    roles: ['SALES_MANAGER', 'FINANCE', 'VP_SALES', 'CPQ_ADMIN'],
  },
  {
    label: 'Exceptions',
    path: '/exceptions',
    roles: ['SALES_MANAGER', 'CPQ_ADMIN'],
  },
  {
    label: 'Oracle CPQ',
    path: '/cpq',
    roles: ['SALES_REP', 'SALES_MANAGER', 'CPQ_ADMIN'],
  },
]

export function canSeeNavigationItem(
  userRoles: UserRole[],
  item: NavigationItem,
): boolean {
  if (!item.roles) {
    return true
  }

  return item.roles.some((role) => userRoles.includes(role))
}
