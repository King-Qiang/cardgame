import viewportAdapt from '../../behaviors/viewport-adapt'
import { buildSeatViews, getMyPlayer, getUserInfoId } from '../../utils/room-seats'
import * as storage from '../../utils/storage'
import * as room from '../../services/room'
import * as ws from '../../services/ws'
import { handleApiError } from '../../services/request'
import type { RoomSeatView } from '../../types/api'

const POLL_INTERVAL_MS = 2000

Page({
  behaviors: [viewportAdapt],
  data: {
    roomId: '',
    gameType: 'DOUDIZHU',
    mode: 'FRIEND',
    status: 'WAITING',
    seats: [] as RoomSeatView[],
    isOwner: false,
    myReady: false,
    canStart: false,
    canAddBot: false,
    showLeaveModal: false,
    actionLoading: false,
  },

  pollTimer: 0 as number,
  polling: false,
  myUserId: undefined as number | undefined,

  onLoad(query: Record<string, string | undefined>) {
    const roomId = query.roomId || ''
    this.myUserId = getUserInfoId()
    this.setData({ roomId })
    if (!roomId) {
      wx.showToast({ title: '房间号无效', icon: 'none' })
      setTimeout(() => wx.reLaunch({ url: '/pages/lobby/lobby' }), 500)
      return
    }
    storage.setLastRoomId(roomId)
    this.refreshRoom()
    this.startPolling()
    this.setupWs(roomId)
  },

  onUnload() {
    this.stopPolling()
    if (this.onRoomEventBound) {
      ws.off('ROOM_EVENT', this.onRoomEventBound)
    }
  },

  onRoomEventBound: null as ((msg: import('../../types/game').WsMessage) => void) | null,

  async setupWs(roomId: string) {
    try {
      await ws.ensureConnected()
      ws.bindRoom(roomId)
      this.onRoomEventBound = () => {
        this.refreshRoom(true)
      }
      ws.on('ROOM_EVENT', this.onRoomEventBound)
    } catch {
      // REST 轮询兜底
    }
  },

  onShareAppMessage() {
    const { roomId } = this.data
    return {
      title: '来一起玩斗地主吧',
      path: `/pages/index/index?roomId=${roomId}`,
    }
  },

  startPolling() {
    this.stopPolling()
    this.pollTimer = setInterval(() => {
      this.refreshRoom(true)
    }, POLL_INTERVAL_MS) as unknown as number
  },

  stopPolling() {
    if (this.pollTimer) {
      clearInterval(this.pollTimer)
      this.pollTimer = 0
    }
  },

  async refreshRoom(silent = false) {
    if (this.polling) return
    this.polling = true
    try {
      const detail = await room.detail(this.data.roomId)
      if (detail.status === 'PLAYING') {
        this.stopPolling()
        wx.redirectTo({
          url: `/pages/game/game?roomId=${encodeURIComponent(detail.roomId)}&gameType=${encodeURIComponent(detail.gameType)}`,
        })
        return
      }
      const seats = buildSeatViews(detail, this.myUserId)
      const me = getMyPlayer(detail, this.myUserId)
      const isOwner = detail.ownerId === this.myUserId
      const allReady = detail.players.length >= detail.maxPlayers
        && detail.players.every((p) => p.ready)
      const robotCount = detail.players.filter((p) => p.isRobot).length
      const canAddBot = isOwner
        && detail.mode === 'FRIEND'
        && detail.status === 'WAITING'
        && detail.players.length < detail.maxPlayers
        && robotCount < 2
      this.setData({
        gameType: detail.gameType,
        mode: detail.mode,
        status: detail.status,
        seats,
        isOwner,
        myReady: !!me?.ready,
        canStart: isOwner && allReady && detail.status === 'WAITING',
        canAddBot,
      })
    } catch (err) {
      if (!silent) {
        await handleApiError(err)
      }
    } finally {
      this.polling = false
    }
  },

  onCopyRoomId() {
    wx.setClipboardData({
      data: this.data.roomId,
      success: () => wx.showToast({ title: '已复制', icon: 'success' }),
    })
  },

  async onToggleReady() {
    if (this.data.actionLoading) return
    this.setData({ actionLoading: true })
    try {
      await room.ready(this.data.roomId, !this.data.myReady)
      await this.refreshRoom(true)
    } catch (err) {
      await handleApiError(err)
    } finally {
      this.setData({ actionLoading: false })
    }
  },

  async onStartGame() {
    if (!this.data.canStart || this.data.actionLoading) return
    this.setData({ actionLoading: true })
    try {
      const detail = await room.start(this.data.roomId)
      if (detail.status === 'PLAYING') {
        this.stopPolling()
        wx.redirectTo({
          url: `/pages/game/game?roomId=${encodeURIComponent(detail.roomId)}&gameType=${encodeURIComponent(detail.gameType)}`,
        })
      }
    } catch (err) {
      await handleApiError(err)
    } finally {
      this.setData({ actionLoading: false })
    }
  },

  onLeaveTap() {
    this.setData({ showLeaveModal: true })
  },

  onLeaveCancel() {
    this.setData({ showLeaveModal: false })
  },

  async onLeaveConfirm() {
    if (this.data.actionLoading) return
    this.setData({ actionLoading: true, showLeaveModal: false })
    this.stopPolling()
    try {
      await room.leave(this.data.roomId)
      storage.clearLastRoomId()
      wx.reLaunch({ url: '/pages/lobby/lobby' })
    } catch (err) {
      await handleApiError(err)
      this.startPolling()
    } finally {
      this.setData({ actionLoading: false })
    }
  },

  async onAddBot() {
    if (!this.data.canAddBot || this.data.actionLoading) return
    this.setData({ actionLoading: true })
    try {
      await room.addBot(this.data.roomId, 1)
      await this.refreshRoom(true)
    } catch (err) {
      await handleApiError(err)
    } finally {
      this.setData({ actionLoading: false })
    }
  },

  async onRemoveBot(e: WechatMiniprogram.TouchEvent) {
    if (!this.data.isOwner || this.data.actionLoading) return
    const seat = e.currentTarget.dataset.seat as number
    if (seat === undefined || seat === null) return
    this.setData({ actionLoading: true })
    try {
      await room.removeBot(this.data.roomId, seat)
      await this.refreshRoom(true)
    } catch (err) {
      await handleApiError(err)
    } finally {
      this.setData({ actionLoading: false })
    }
  },
})
