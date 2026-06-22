import client from './client'
import type { ApiResponse, PageResult } from '../types/api'

export interface RecordListItem {
  recordId: string
  roomId: string
  gameType: string
  mode?: string
  status: string
  startAt: string
  endAt?: string
}

export interface GameActionLogItem {
  id: number
  recordId: string
  seq: number
  userId: number
  action: string
  payload?: Record<string, unknown>
  createdAt: string
}

export interface RecordDetail {
  recordId: string
  roomId: string
  gameType: string
  status: string
  startAt: string
  endAt?: string
  resultJson?: Record<string, unknown>
  actions: GameActionLogItem[]
}

export async function fetchRecords(params: Record<string, unknown>) {
  const res = await client.get<ApiResponse<PageResult<RecordListItem>>>('/admin/records', { params })
  return res.data.data
}

export async function fetchRecordDetail(recordId: string) {
  const res = await client.get<ApiResponse<RecordDetail>>(`/admin/records/${recordId}`)
  return res.data.data
}

export async function fetchRecordReplay(recordId: string) {
  const res = await client.get<ApiResponse<GameActionLogItem[]>>(`/admin/records/${recordId}/replay`)
  return res.data.data
}
