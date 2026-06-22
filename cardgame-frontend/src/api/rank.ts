import client from './client'
import type { ApiResponse, PageResult } from '../types/api'

export interface RankLeaderboardItem {
  rank: number
  userId: number
  nickname: string
  tier: string
  points: number
  wins: number
  losses: number
}

export interface PlayerRankInfo {
  userId: number
  gameType: string
  seasonId: string
  tier: string
  points: number
  wins: number
  losses: number
  nextTier?: string | null
  pointsToNextTier?: number | null
}

export interface PlayerRankLogItem {
  id: number
  recordId: string
  deltaPoints: number
  tierBefore: string
  tierAfter: string
  pointsBefore: number
  pointsAfter: number
  createdAt: string
}

export async function fetchRankLeaderboard(params: Record<string, unknown>) {
  const res = await client.get<ApiResponse<PageResult<RankLeaderboardItem>>>('/admin/rank/leaderboard', {
    params,
  })
  return res.data.data
}

export async function fetchUserRank(userId: number, gameType = 'DOUDIZHU') {
  const res = await client.get<ApiResponse<PlayerRankInfo>>(`/admin/rank/users/${userId}`, {
    params: { gameType },
  })
  return res.data.data
}

export async function fetchUserRankLogs(userId: number, gameType = 'DOUDIZHU') {
  const res = await client.get<ApiResponse<PlayerRankLogItem[]>>(`/admin/rank/users/${userId}/logs`, {
    params: { gameType },
  })
  return res.data.data
}

export async function fetchCurrentSeasonId() {
  const res = await client.get<ApiResponse<string>>('/admin/rank/season')
  return res.data.data
}
