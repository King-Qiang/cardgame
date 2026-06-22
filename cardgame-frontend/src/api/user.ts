import client from './client'
import type { ApiResponse, PageResult } from '../types/api'

export interface UserListItem {
  id: number
  nickname: string
  avatar: string
  openidMasked: string
  gold: number
  rankTier?: string
  rankPoints?: number
  statusLabel: string
  createdAt: string
}

export async function fetchUsers(params: Record<string, unknown>) {
  const res = await client.get<ApiResponse<PageResult<UserListItem>>>('/admin/users', { params })
  return res.data.data
}

export async function banUser(id: number, reason: string) {
  await client.post(`/admin/users/${id}/ban`, { reason })
}

export async function unbanUser(id: number) {
  await client.post(`/admin/users/${id}/unban`)
}
