import client from './client'
import type { ApiResponse } from '../types/api'

export interface DashboardOverview {
  todayDau: number
  onlineCount: number
  todayGames: number
  todayRechargeAmount: number
}

export interface DashboardTrends {
  dates: string[]
  games: number[]
  newUsers: number[]
  revenue: number[]
}

export interface DashboardAlert {
  level: 'WARNING' | 'INFO'
  code: string
  message: string
  count: number
  link: string
}

export interface DashboardAlerts {
  alerts: DashboardAlert[]
}

export async function fetchOverview() {
  const res = await client.get<ApiResponse<DashboardOverview>>('/admin/dashboard/overview')
  return res.data.data
}

export async function fetchTrends(days = 7) {
  const res = await client.get<ApiResponse<DashboardTrends>>('/admin/dashboard/trends', { params: { days } })
  return res.data.data
}

export async function fetchAlerts() {
  const res = await client.get<ApiResponse<DashboardAlerts>>('/admin/dashboard/alerts')
  return res.data.data
}
