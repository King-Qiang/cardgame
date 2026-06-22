import type { QuickMatchRequest, QuickMatchResponse } from '../types/api'
import * as request from './request'

export function quick(body: QuickMatchRequest): Promise<QuickMatchResponse> {
  return request.post<QuickMatchResponse>('/match/quick', body)
}

export function status(gameType: string): Promise<QuickMatchResponse> {
  return request.get<QuickMatchResponse>('/match/status', { gameType })
}

export function cancel(gameType: string): Promise<QuickMatchResponse> {
  return request.del<QuickMatchResponse>('/match/quick', { gameType })
}
