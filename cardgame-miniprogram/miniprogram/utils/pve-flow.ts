import type { GameType } from '../config/game'
import * as room from '../services/room'
import * as storage from './storage'
import * as ws from '../services/ws'

export async function enterPveGame(gameType: GameType = 'DOUDIZHU'): Promise<void> {
  const detail = await room.createPve({
    gameType,
    config: { baseScore: 1, botDifficulty: 'EASY' },
  })
  storage.setLastRoomId(detail.roomId)
  try {
    await ws.ensureConnected()
    ws.bindRoom(detail.roomId)
  } catch {
    // 牌桌 REST/WS 兜底
  }
  wx.hideLoading()
  wx.redirectTo({
    url: `/pages/game/game?roomId=${encodeURIComponent(detail.roomId)}&gameType=${encodeURIComponent(detail.gameType)}`,
  })
}
