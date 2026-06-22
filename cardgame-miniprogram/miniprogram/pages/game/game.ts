import viewportAdapt from '../../behaviors/viewport-adapt'
import { TURN_TIMEOUT_SEC } from '../../config/game'
import type { IAppOption } from '../../app'
import * as room from '../../services/room'
import * as ws from '../../services/ws'
import { handleApiError } from '../../services/request'
import type {
  ActionResultPayload,
  DoudizhuStateSync,
  OpponentView,
  SettlementPayload,
  WsMessage,
} from '../../types/game'
import type { PendingSettlement } from '../../types/api'
import { sortHand } from '../../utils/cards'
import {
  buildOpponents,
  buildPlayerMap,
  canPassInPlaying,
  canPlayCards,
  isMyTurn,
} from '../../utils/doudizhu-view'
import * as storage from '../../utils/storage'
import { resolvePlayZoneView, type OptimisticPlay } from '../../utils/play-zone'
import {
  applyActedUserToSeatTable,
  createEmptySeatTable,
  type SeatTableMap,
} from '../../utils/seat-table'

const SETTLE_ANIM_MS = 1500

Page({
  behaviors: [viewportAdapt],
  data: {
    roomId: '',
    gameType: 'DOUDIZHU',
    connected: false,
    multiplier: 1,
    turnSeconds: TURN_TIMEOUT_SEC,
    turnUrgent: false,
    phase: 'BIDDING' as string,
    phaseLabel: '叫地主',
    myTurn: false,
    canPass: false,
    canPlay: false,
    myHand: [] as string[],
    selectedCards: [] as string[],
    leftOpponent: null as OpponentView | null,
    rightOpponent: null as OpponentView | null,
    playZoneFaceCards: [] as string[],
    playZoneBackCount: 0,
    playZoneSubtitle: '',
    actionLoading: false,
    showExitModal: false,
    disconnectOverlay: false,
  },

  roomMode: 'FRIEND',
  playerMap: null as Map<number, import('../../types/api').RoomPlayer> | null,
  seatTableMap: createEmptySeatTable() as SeatTableMap,
  turnTimer: 0 as number,
  turnTimeoutHandled: false,
  lastCurrentSeat: -1,
  settling: false,
  lastActedUserId: 0,
  mySeat: undefined as number | undefined,
  optimisticPlay: null as OptimisticPlay | null,
  cachedOwnPlayCards: [] as string[],

  onStateSyncBound: null as ((msg: WsMessage) => void) | null,
  onActionResultBound: null as ((msg: WsMessage) => void) | null,
  onSettlementBound: null as ((msg: WsMessage) => void) | null,
  onErrorBound: null as ((msg: WsMessage) => void) | null,

  onLoad(query: Record<string, string | undefined>) {
    const roomId = query.roomId || ''
    const gameType = query.gameType || 'DOUDIZHU'
    this.setData({ roomId, gameType })
    if (!roomId) {
      wx.showToast({ title: '房间号无效', icon: 'none' })
      setTimeout(() => wx.reLaunch({ url: '/pages/lobby/lobby' }), 500)
      return
    }
    storage.setLastRoomId(roomId)
    this.bindWsHandlers()
    this.bootstrap(roomId)
  },

  onUnload() {
    this.stopTurnTimer()
    this.unbindWsHandlers()
  },

  bindWsHandlers() {
    this.onStateSyncBound = (msg) => this.handleStateSync(msg)
    this.onActionResultBound = (msg) => this.handleActionResult(msg)
    this.onSettlementBound = (msg) => this.handleSettlement(msg)
    this.onErrorBound = (msg) => {
      const payload = msg.payload as { message?: string }
      wx.showToast({ title: payload.message || '对局错误', icon: 'none' })
    }
    ws.on('STATE_SYNC', this.onStateSyncBound)
    ws.on('ACTION_RESULT', this.onActionResultBound)
    ws.on('SETTLEMENT', this.onSettlementBound)
    ws.on('ERROR', this.onErrorBound)
  },

  unbindWsHandlers() {
    if (this.onStateSyncBound) ws.off('STATE_SYNC', this.onStateSyncBound)
    if (this.onActionResultBound) ws.off('ACTION_RESULT', this.onActionResultBound)
    if (this.onSettlementBound) ws.off('SETTLEMENT', this.onSettlementBound)
    if (this.onErrorBound) ws.off('ERROR', this.onErrorBound)
  },

  async bootstrap(roomId: string) {
    wx.showLoading({ title: '进入牌桌…', mask: true })
    try {
      const detail = await room.detail(roomId)
      this.roomMode = detail.mode
      this.playerMap = buildPlayerMap(detail.players)
      await ws.ensureConnected()
      this.setData({ connected: true, disconnectOverlay: false })
      ws.reconnect(roomId)
    } catch (err) {
      await handleApiError(err)
      setTimeout(() => wx.reLaunch({ url: '/pages/lobby/lobby' }), 800)
    } finally {
      wx.hideLoading()
    }
  },

  handleStateSync(msg: WsMessage) {
    const state = msg.payload as DoudizhuStateSync
    if (!state || msg.roomId !== this.data.roomId) {
      return
    }
    ws.syncActionSeq(state.actionSeq)

    const phaseBefore = this.data.phase
    if (phaseBefore === 'BIDDING' && state.phase === 'PLAYING') {
      this.seatTableMap = createEmptySeatTable()
    }
    if (this.lastActedUserId) {
      const actedSeat = this.findSeatByUserId(this.lastActedUserId)
      if (actedSeat != null) {
        this.seatTableMap = applyActedUserToSeatTable(
          this.seatTableMap,
          state,
          actedSeat,
          phaseBefore,
        )
      }
      this.lastActedUserId = 0
    }
    if (state.phase === 'FINISHED') {
      this.seatTableMap = createEmptySeatTable()
    }

    this.applyState(state)
  },

  handleActionResult(msg: WsMessage) {
    const payload = msg.payload as ActionResultPayload
    if (!payload.success) {
      wx.showToast({ title: payload.message || '操作失败', icon: 'none' })
      this.setData({ actionLoading: false })
      return
    }
    this.lastActedUserId = payload.userId
    this.setData({ actionLoading: false, selectedCards: [] })
  },

  handleSettlement(msg: WsMessage) {
    if (this.settling) return
    this.settling = true
    this.stopTurnTimer()

    const payload = msg.payload as SettlementPayload
    const pending: PendingSettlement = {
      roomId: msg.roomId || this.data.roomId,
      gameType: this.data.gameType,
      mode: this.roomMode,
      settlements: payload.settlements,
      result: payload.result,
    }

    const app = getApp<IAppOption>()
    app.globalData.pendingSettlement = pending
    storage.clearLastRoomId()

    wx.showToast({ title: '对局结束', icon: 'success' })
    setTimeout(() => {
      wx.redirectTo({ url: '/pages/result/result' })
    }, SETTLE_ANIM_MS)
  },

  applyState(state: DoudizhuStateSync) {
    const playerMap = this.playerMap || new Map()
    this.mySeat = state.mySeat
    const myHand = sortHand(state.myHand || [])
    const selected = this.data.selectedCards.filter((c) => myHand.includes(c))
    const opponents = buildOpponents(state, playerMap, state.currentSeat, this.seatTableMap)
    const phaseLabel = state.phase === 'BIDDING' ? '叫地主阶段' : ''

    const { view, nextCachedOwnCards } = resolvePlayZoneView(
      state,
      state.mySeat,
      this.optimisticPlay,
      this.cachedOwnPlayCards,
    )
    this.cachedOwnPlayCards = nextCachedOwnCards
    if (state.lastPlay && this.optimisticPlay && state.lastPlay.seat === this.optimisticPlay.seat) {
      this.optimisticPlay = null
    }
    if (!state.lastPlay) {
      this.optimisticPlay = null
      this.cachedOwnPlayCards = []
    }

    if (state.currentSeat !== this.lastCurrentSeat) {
      this.lastCurrentSeat = state.currentSeat
      this.resetTurnTimer()
    }

    this.setData({
      phase: state.phase,
      phaseLabel,
      multiplier: state.multiplier || 1,
      myHand,
      selectedCards: selected,
      playZoneFaceCards: view.faceCards,
      playZoneBackCount: view.backCount,
      playZoneSubtitle: view.subtitle,
      leftOpponent: opponents[0] || null,
      rightOpponent: opponents[1] || null,
      myTurn: isMyTurn(state),
      canPass: canPassInPlaying(state),
      canPlay: canPlayCards(state, selected.length),
      actionLoading: false,
    })
  },

  resetTurnTimer() {
    this.stopTurnTimer()
    this.turnTimeoutHandled = false
    this.setData({ turnSeconds: TURN_TIMEOUT_SEC, turnUrgent: false })
    this.turnTimer = setInterval(() => {
      const next = this.data.turnSeconds - 1
      if (next <= 0) {
        this.setData({ turnSeconds: 0, turnUrgent: true })
        this.stopTurnTimer()
        this.handleTurnTimeout()
        return
      }
      this.setData({
        turnSeconds: next,
        turnUrgent: next <= 5,
      })
    }, 1000) as unknown as number
  },

  /** 本地倒计时归零：轮到自己时自动不叫 / 不出 / 领出最小单牌 */
  handleTurnTimeout() {
    if (this.turnTimeoutHandled) return
    if (!this.data.myTurn || this.data.actionLoading) return
    const phase = this.data.phase
    if (phase !== 'BIDDING' && phase !== 'PLAYING') return

    this.turnTimeoutHandled = true

    if (phase === 'BIDDING') {
      wx.showToast({ title: '时间到，自动不叫', icon: 'none' })
      this.sendAction('PASS')
      return
    }

    if (this.data.canPass) {
      wx.showToast({ title: '时间到，自动不出', icon: 'none' })
      this.sendAction('PASS')
      return
    }

    const hand = this.data.myHand
    if (hand.length > 0) {
      const card = hand[hand.length - 1]
      wx.showToast({ title: '时间到，自动出牌', icon: 'none' })
      this.sendAction('PLAY_CARDS', [card])
    }
  },

  stopTurnTimer() {
    if (this.turnTimer) {
      clearInterval(this.turnTimer)
      this.turnTimer = 0
    }
  },

  findSeatByUserId(userId: number): number | null {
    if (!this.playerMap) return null
    for (const player of this.playerMap.values()) {
      if (player.userId === userId) {
        return player.seat
      }
    }
    return null
  },

  onToggleCard(e: WechatMiniprogram.CustomEvent) {
    const code = e.detail.code as string
    const selected = [...this.data.selectedCards]
    const idx = selected.indexOf(code)
    if (idx >= 0) {
      selected.splice(idx, 1)
    } else {
      selected.push(code)
    }
    this.setData({
      selectedCards: selected,
      canPlay: this.data.myTurn && this.data.phase === 'PLAYING' && selected.length > 0,
    })
  },

  async sendAction(action: string, cards?: string[]) {
    if (this.data.actionLoading) return
    if (action === 'PLAY_CARDS' && cards && cards.length > 0 && this.mySeat != null) {
      this.optimisticPlay = { seat: this.mySeat, cards: [...cards] }
    }
    this.setData({ actionLoading: true })
    ws.sendAction(action, cards)
    if (action === 'PLAY_CARDS') {
      wx.vibrateShort({ type: 'light' })
    }
  },

  onPass() {
    if (!this.data.myTurn) return
    if (this.data.phase === 'BIDDING') {
      this.sendAction('PASS')
      return
    }
    if (!this.data.canPass) return
    this.sendAction('PASS')
  },

  onCall() {
    if (!this.data.myTurn || this.data.phase !== 'BIDDING') return
    this.sendAction('CALL_LANDLORD')
  },

  onPlay() {
    if (!this.data.canPlay) return
    this.sendAction('PLAY_CARDS', this.data.selectedCards)
  },

  onExitTap() {
    this.setData({ showExitModal: true })
  },

  onExitCancel() {
    this.setData({ showExitModal: false })
  },

  onExitConfirm() {
    this.setData({ showExitModal: false })
    storage.clearLastRoomId()
    wx.reLaunch({ url: '/pages/lobby/lobby' })
  },
})
