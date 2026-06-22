import { GAME_CATALOG, type GameType } from '../config/game'
import * as room from '../services/room'
import * as storage from './storage'
import type { RoomDetail } from '../types/api'

import { safeRedirectTo } from './navigate'

export type PostLoginRoute =
  | { type: 'lobby' }
  | { type: 'room'; roomId: string }
  | { type: 'game'; roomId: string; gameType: string }

function gameTypeLabel(gameType: string): string {
  const meta = GAME_CATALOG[gameType as GameType]
  return meta?.name || gameType
}

export function showRejoinModal(roomId: string, detail: RoomDetail): Promise<boolean> {
  return new Promise((resolve) => {
    wx.showModal({
      title: '进行中的对局',
      content: `房间 ${roomId} · ${gameTypeLabel(detail.gameType)} · 是否回到牌桌？`,
      confirmText: '回到牌桌',
      cancelText: '取消',
      success: (res) => resolve(!!res.confirm),
      fail: () => resolve(false),
    })
  })
}

export async function resolvePostLoginRoute(): Promise<PostLoginRoute> {
  const lastRoomId = storage.getLastRoomId().trim()
  if (!lastRoomId) {
    return { type: 'lobby' }
  }

  try {
    const detail = await room.detail(lastRoomId)
    if (detail.status === 'PLAYING') {
      const ok = await showRejoinModal(lastRoomId, detail)
      if (ok) {
        return { type: 'game', roomId: lastRoomId, gameType: detail.gameType }
      }
      storage.clearLastRoomId()
      return { type: 'lobby' }
    }
    if (detail.status === 'WAITING') {
      return { type: 'room', roomId: lastRoomId }
    }
    storage.clearLastRoomId()
    return { type: 'lobby' }
  } catch {
    storage.clearLastRoomId()
    return { type: 'lobby' }
  }
}

export function navigatePostLoginRoute(route: PostLoginRoute): void {
  if (route.type === 'room') {
    if (!route.roomId.trim()) {
      safeRedirectTo('/pages/lobby/lobby')
      return
    }
    safeRedirectTo(`/pages/room/room?roomId=${encodeURIComponent(route.roomId)}`)
    return
  }
  if (route.type === 'game') {
    if (!route.roomId.trim()) {
      safeRedirectTo('/pages/lobby/lobby')
      return
    }
    safeRedirectTo(
      `/pages/game/game?roomId=${encodeURIComponent(route.roomId)}&gameType=${encodeURIComponent(route.gameType)}`,
    )
    return
  }
  safeRedirectTo('/pages/lobby/lobby')
}
