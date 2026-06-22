import type { GameActionLog, PageResult, PlayerRecordDetail, PlayerRecordListItem } from '../types/api'
import * as request from './request'

export function list(params?: {
  page?: number
  pageSize?: number
  gameType?: string
  mode?: string
}): Promise<PageResult<PlayerRecordListItem>> {
  return request.get<PageResult<PlayerRecordListItem>>('/records', {
    page: params?.page ?? 1,
    pageSize: params?.pageSize ?? 20,
    gameType: params?.gameType,
    mode: params?.mode,
  })
}

export function detail(recordId: string): Promise<PlayerRecordDetail> {
  return request.get<PlayerRecordDetail>(`/records/${recordId}`)
}

export function replay(recordId: string): Promise<GameActionLog[]> {
  return request.get<GameActionLog[]>(`/records/${recordId}/replay`)
}
