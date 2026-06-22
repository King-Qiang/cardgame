import client from './client'
import type { ApiResponse, PageResult } from '../types/api'

export interface ActivityItem {
  id: number
  code: string
  name: string
  type: string
  configJson: unknown
  status: number
  statusLabel: string
  startAt?: string
  endAt?: string
  createdAt: string
  updatedAt: string
}

export interface ActivityPayload {
  code: string
  name: string
  type: string
  configJson: unknown
  status: number
  startAt?: string | null
  endAt?: string | null
}

export async function fetchActivities(params: Record<string, unknown>) {
  const res = await client.get<ApiResponse<PageResult<ActivityItem>>>('/admin/activities', { params })
  return res.data.data
}

export async function createActivity(payload: ActivityPayload) {
  const res = await client.post<ApiResponse<ActivityItem>>('/admin/activities', payload)
  return res.data.data
}

export async function updateActivity(id: number, payload: ActivityPayload) {
  const res = await client.put<ApiResponse<ActivityItem>>(`/admin/activities/${id}`, payload)
  return res.data.data
}

export async function deleteActivity(id: number) {
  await client.delete(`/admin/activities/${id}`)
}
