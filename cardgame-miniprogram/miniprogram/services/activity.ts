import type { DailySignStatus } from '../types/api'
import * as request from './request'

export function dailySignStatus(): Promise<DailySignStatus> {
  return request.get<DailySignStatus>('/activities/daily-sign')
}

export function dailySign(): Promise<DailySignStatus> {
  return request.post<DailySignStatus>('/activities/daily-sign')
}
