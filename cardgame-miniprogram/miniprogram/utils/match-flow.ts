import type { QuickMatchResponse } from '../types/api'

export function navigateByMatchStatus(res: QuickMatchResponse, gameType: string): boolean {
  if (res.status === 'MATCHED' && res.roomId) {
    wx.redirectTo({ url: `/pages/room/room?roomId=${encodeURIComponent(res.roomId)}` })
    return true
  }
  if (res.status === 'WAITING') {
    const mode = res.matchMode || 'MATCH'
    wx.navigateTo({
      url: `/pages/match/match?gameType=${encodeURIComponent(gameType)}&mode=${encodeURIComponent(mode)}`,
    })
    return true
  }
  return false
}

export function buildMatchStatusText(res: QuickMatchResponse, matchLabel: string): string {
  if (res.status === 'WAITING') {
    const parts = [`队列 ${res.queueSize}/${res.requiredPlayers}`, matchLabel]
    if (res.estimatedWaitSec != null && res.estimatedWaitSec > 0) {
      parts.push(`预计 ${res.estimatedWaitSec}s`)
    }
    if (res.matchTier) {
      parts.push(`段位 ${res.matchTier}`)
    }
    return parts.join(' · ')
  }
  return matchLabel
}
