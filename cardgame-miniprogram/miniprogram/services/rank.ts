import type { GameType } from '../config/game'
import type { PageResult, PlayerRank, RankLeaderboardItem } from '../types/api'
import * as request from './request'

export function me(gameType: GameType = 'DOUDIZHU'): Promise<PlayerRank> {
  return request.get<PlayerRank>('/rank/me', { gameType })
}

export function leaderboard(
  gameType: GameType = 'DOUDIZHU',
  page = 1,
  pageSize = 20,
): Promise<PageResult<RankLeaderboardItem>> {
  return request.get<PageResult<RankLeaderboardItem>>('/rank/leaderboard', {
    gameType,
    page,
    pageSize,
  })
}
