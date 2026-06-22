import viewportAdapt from '../../behaviors/viewport-adapt'
import { GAME_CATALOG, type GameType } from '../../config/game'
import { buildMatchStatusText, navigateByMatchStatus } from '../../utils/match-flow'
import { tierLabel } from '../../utils/format'
import * as match from '../../services/match'
import * as ws from '../../services/ws'
import { handleApiError, ApiError } from '../../services/request'
import type { MatchMode } from '../../types/api'
import type { RoomEventPayload, WsMessage } from '../../types/game'

const POLL_INTERVAL_MS = 3000

Page({
  behaviors: [viewportAdapt],
  data: {
    gameType: 'DOUDIZHU' as GameType,
    mode: 'MATCH' as MatchMode,
    matchLabel: '3 人局',
    statusText: '正在匹配…',
    subText: '',
    cancelling: false,
    wsConnected: false,
  },

  pollTimer: 0 as number,
  polling: false,
  keepWs: false,
  onRoomEventBound: null as ((msg: WsMessage) => void) | null,

  onLoad(query: Record<string, string | undefined>) {
    const gameType = (query.gameType || 'DOUDIZHU') as GameType
    const mode = (query.mode || 'MATCH') as MatchMode
    const meta = GAME_CATALOG[gameType]
    this.setData({
      gameType,
      mode,
      matchLabel: meta?.matchLabel || '匹配中',
      statusText: mode === 'RANKED' ? '排位匹配中…' : '正在匹配…',
    })
    this.preconnectWs()
    this.pollOnce()
    this.startPolling()
  },

  onUnload() {
    this.stopPolling()
    if (this.onRoomEventBound) {
      ws.off('ROOM_EVENT', this.onRoomEventBound)
    }
    if (!this.keepWs) {
      ws.close()
    }
  },

  async preconnectWs() {
    try {
      await ws.ensureConnected()
      this.setData({ wsConnected: true })
      this.onRoomEventBound = (msg) => this.handleRoomEvent(msg)
      ws.on('ROOM_EVENT', this.onRoomEventBound)
    } catch {
      this.setData({ wsConnected: false })
    }
  },

  handleRoomEvent(msg: WsMessage) {
    const payload = msg.payload as RoomEventPayload
    if (payload.event !== 'MATCH_FOUND' || !msg.roomId) {
      return
    }
    this.keepWs = true
    ws.bindRoom(msg.roomId)
    this.stopPolling()
    wx.redirectTo({ url: `/pages/room/room?roomId=${encodeURIComponent(msg.roomId)}` })
  },

  startPolling() {
    this.stopPolling()
    this.pollTimer = setInterval(() => {
      this.pollOnce()
    }, POLL_INTERVAL_MS) as unknown as number
  },

  stopPolling() {
    if (this.pollTimer) {
      clearInterval(this.pollTimer)
      this.pollTimer = 0
    }
  },

  async pollOnce() {
    if (this.polling) return
    this.polling = true
    try {
      const res = await match.status(this.data.gameType)
      if (res.status === 'MATCHED') {
        this.stopPolling()
        this.keepWs = true
        if (res.roomId) {
          try {
            await ws.ensureConnected()
            ws.bindRoom(res.roomId)
            this.setData({ wsConnected: true })
          } catch {
            // REST 跳转仍可用
          }
        }
        navigateByMatchStatus(res, this.data.gameType)
        return
      }
      if (res.status === 'NOT_IN_QUEUE' || res.status === 'CANCELLED') {
        this.stopPolling()
        wx.showToast({ title: '匹配已结束', icon: 'none' })
        setTimeout(() => wx.reLaunch({ url: '/pages/lobby/lobby' }), 500)
        return
      }
      const subText = buildMatchStatusText(res, this.data.matchLabel)
      const tierHint = res.matchTier ? tierLabel(res.matchTier) : ''
      this.setData({
        subText,
        statusText: this.data.mode === 'RANKED' && tierHint
          ? `排位匹配 · ${tierHint}`
          : '正在匹配…',
      })
    } catch (err) {
      await handleApiError(err)
    } finally {
      this.polling = false
    }
  },

  async onCancel() {
    if (this.data.cancelling) return
    this.setData({ cancelling: true })
    this.stopPolling()
    this.keepWs = false
    try {
      await match.cancel(this.data.gameType)
    } catch (err) {
      if (!(err instanceof ApiError && err.code === 40006)) {
        await handleApiError(err)
      }
    } finally {
      ws.close()
      wx.reLaunch({ url: '/pages/lobby/lobby' })
    }
  },
})
