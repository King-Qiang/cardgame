import type { UserInfo } from '../types/api'
import * as user from '../services/user'
import * as wallet from '../services/wallet'
import * as rank from '../services/rank'
import { tierLabel } from './format'
import * as storage from './storage'

export interface ProfileSummary {
  user: UserInfo | null
  gold: number
  tierText: string
  rankStats: string
}

export async function loadProfileSummary(gameType = 'DOUDIZHU'): Promise<ProfileSummary> {
  try {
    const me = await user.me(gameType)
    const userInfo: UserInfo = { id: me.id, nickname: me.nickname, avatar: me.avatar }
    storage.setUserInfo(userInfo)
    const rankSummary = me.rankSummary
    const tierText = rankSummary
      ? `${tierLabel(rankSummary.tier)} ${rankSummary.points}分`
      : '暂无段位'
    const rankStats = rankSummary ? `${rankSummary.wins}胜${rankSummary.losses}负` : ''
    return { user: userInfo, gold: me.gold ?? 0, tierText, rankStats }
  } catch {
    // fallback：旧版分散请求
  }

  const cached = storage.getUserInfo<UserInfo>()
  let gold = 0
  let tierText = '—'
  let rankStats = ''

  try {
    const balance = await wallet.balance()
    gold = balance.gold ?? 0
  } catch {
    // ignore
  }

  try {
    const rankInfo = await rank.me(gameType as 'DOUDIZHU')
    tierText = `${tierLabel(rankInfo.tier)} ${rankInfo.points}分`
    rankStats = `${rankInfo.wins}胜${rankInfo.losses}负`
  } catch {
    tierText = '暂无段位'
  }

  return { user: cached, gold, tierText, rankStats }
}
