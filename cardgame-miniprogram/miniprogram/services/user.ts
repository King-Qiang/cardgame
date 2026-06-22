import type { PlayerMe, UpdatePlayerProfileRequest } from '../types/api'
import * as request from './request'

export function me(gameType = 'DOUDIZHU'): Promise<PlayerMe> {
  return request.get<PlayerMe>('/user/me', { gameType })
}

export function updateProfile(body: UpdatePlayerProfileRequest, gameType = 'DOUDIZHU'): Promise<PlayerMe> {
  return request.put<PlayerMe>('/user/profile', body, { query: { gameType } })
}
