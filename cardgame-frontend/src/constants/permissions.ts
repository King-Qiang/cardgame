export const PERMISSIONS = {
  DASHBOARD_VIEW: 'dashboard:view',
  USER_LIST: 'user:list',
  USER_BAN: 'user:ban',
  USER_ADJUST: 'user:adjust',
  ROOM_MONITOR: 'room:monitor',
  ROOM_KICK: 'room:kick',
  ROOM_DISBAND: 'room:disband',
  RECORD_LIST: 'record:list',
  ECONOMY_TRANSACTION: 'economy:transaction',
  ECONOMY_ADJUST: 'economy:adjust',
  SHOP_MANAGE: 'shop:manage',
  SYSTEM_ADMIN: 'system:admin',
  SYSTEM_ROLE: 'system:role',
  SYSTEM_LOG: 'system:log',
  SYSTEM_CONFIG: 'system:config',
} as const

export type Permission = (typeof PERMISSIONS)[keyof typeof PERMISSIONS]

export interface MenuRouteMeta {
  title: string
  permission?: Permission
  icon?: string
  hidden?: boolean
}
