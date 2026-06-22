import type { RouteRecordRaw } from 'vue-router'
import { PERMISSIONS } from '../constants/permissions'
import { hasPermission } from '../utils/auth'

export interface AppMenuItem {
  path: string
  title: string
  permission?: string
}

export const appRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/LoginView.vue'),
    meta: { public: true },
  },
  {
    path: '/',
    component: () => import('../layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/DashboardView.vue'),
        meta: { title: '仪表盘', permission: PERMISSIONS.DASHBOARD_VIEW },
      },
      {
        path: 'users',
        name: 'UserList',
        component: () => import('../views/user/UserListView.vue'),
        meta: { title: '用户列表', permission: PERMISSIONS.USER_LIST },
      },
      {
        path: 'rank/leaderboard',
        name: 'RankLeaderboard',
        component: () => import('../views/rank/RankLeaderboardView.vue'),
        meta: { title: '段位排行榜', permission: PERMISSIONS.USER_LIST },
      },
      {
        path: 'rooms',
        name: 'RoomMonitor',
        component: () => import('../views/room/RoomMonitorView.vue'),
        meta: { title: '房间监控', permission: PERMISSIONS.ROOM_MONITOR },
      },
      {
        path: 'records',
        name: 'RecordList',
        component: () => import('../views/record/RecordListView.vue'),
        meta: { title: '对局记录', permission: PERMISSIONS.RECORD_LIST },
      },
      {
        path: 'records/:recordId',
        name: 'RecordDetail',
        component: () => import('../views/record/RecordDetailView.vue'),
        meta: { title: '对局详情', permission: PERMISSIONS.RECORD_LIST, hidden: true },
      },
      {
        path: 'economy/transactions',
        name: 'TransactionList',
        component: () => import('../views/economy/TransactionListView.vue'),
        meta: { title: '金币流水', permission: PERMISSIONS.ECONOMY_TRANSACTION },
      },
      {
        path: 'economy/orders',
        name: 'OrderList',
        component: () => import('../views/economy/OrderListView.vue'),
        meta: { title: '充值订单', permission: PERMISSIONS.ECONOMY_TRANSACTION },
      },
      {
        path: 'economy/adjust-requests',
        name: 'AdjustRequestList',
        component: () => import('../views/economy/AdjustRequestView.vue'),
        meta: { title: '调账审批', permission: PERMISSIONS.ECONOMY_ADJUST },
      },
      {
        path: 'system/configs',
        name: 'SystemConfig',
        component: () => import('../views/system/SystemConfigView.vue'),
        meta: { title: '系统配置', permission: PERMISSIONS.SYSTEM_CONFIG },
      },
      {
        path: 'shop/items',
        name: 'ShopItems',
        component: () => import('../views/shop/ShopItemView.vue'),
        meta: { title: '商城管理', permission: PERMISSIONS.SHOP_MANAGE },
      },
      {
        path: 'activities',
        name: 'Activities',
        component: () => import('../views/activity/ActivityView.vue'),
        meta: { title: '活动管理', permission: PERMISSIONS.SHOP_MANAGE },
      },
      {
        path: 'system/roles',
        name: 'Roles',
        component: () => import('../views/system/RoleView.vue'),
        meta: { title: '角色管理', permission: PERMISSIONS.SYSTEM_ROLE },
      },
      {
        path: 'system/admins',
        name: 'AdminUsers',
        component: () => import('../views/system/AdminUserView.vue'),
        meta: { title: '管理员账号', permission: PERMISSIONS.SYSTEM_ADMIN },
      },
      {
        path: 'system/operation-logs',
        name: 'OperationLogs',
        component: () => import('../views/system/OperationLogView.vue'),
        meta: { title: '操作日志', permission: PERMISSIONS.SYSTEM_LOG },
      },
    ],
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('../views/error/403View.vue'),
    meta: { public: true },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/error/404View.vue'),
    meta: { public: true },
  },
]

export function getAccessibleMenus(): AppMenuItem[] {
  return [
    { path: '/dashboard', title: '仪表盘', permission: PERMISSIONS.DASHBOARD_VIEW },
    { path: '/users', title: '用户管理', permission: PERMISSIONS.USER_LIST },
    { path: '/rank/leaderboard', title: '段位排行榜', permission: PERMISSIONS.USER_LIST },
    { path: '/rooms', title: '房间监控', permission: PERMISSIONS.ROOM_MONITOR },
    { path: '/records', title: '对局记录', permission: PERMISSIONS.RECORD_LIST },
    { path: '/economy/transactions', title: '金币流水', permission: PERMISSIONS.ECONOMY_TRANSACTION },
    { path: '/economy/orders', title: '充值订单', permission: PERMISSIONS.ECONOMY_TRANSACTION },
    { path: '/economy/adjust-requests', title: '调账审批', permission: PERMISSIONS.ECONOMY_ADJUST },
    { path: '/shop/items', title: '商城管理', permission: PERMISSIONS.SHOP_MANAGE },
    { path: '/activities', title: '活动管理', permission: PERMISSIONS.SHOP_MANAGE },
    { path: '/system/configs', title: '系统配置', permission: PERMISSIONS.SYSTEM_CONFIG },
    { path: '/system/roles', title: '角色管理', permission: PERMISSIONS.SYSTEM_ROLE },
    { path: '/system/admins', title: '管理员账号', permission: PERMISSIONS.SYSTEM_ADMIN },
    { path: '/system/operation-logs', title: '操作日志', permission: PERMISSIONS.SYSTEM_LOG },
  ].filter((item) => hasPermission(item.permission))
}

export default appRoutes
