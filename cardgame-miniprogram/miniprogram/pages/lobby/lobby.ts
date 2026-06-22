import viewportAdapt from '../../behaviors/viewport-adapt'
import { GAME_CATALOG, type GameType } from '../../config/game'
import { loadProfileSummary } from '../../utils/profile-summary'
import { navigateByMatchStatus } from '../../utils/match-flow'
import { enterPveGame } from '../../utils/pve-flow'
import * as storage from '../../utils/storage'
import * as match from '../../services/match'
import * as room from '../../services/room'
import { handleApiError, ApiError } from '../../services/request'
import type { MatchMode } from '../../types/api'

Page({
  behaviors: [viewportAdapt],
  data: {
    nickname: '玩家',
    avatarText: '?',
    gold: 0,
    tierText: '—',
    gameType: 'DOUDIZHU' as GameType,
    gameTabs: [
      { gameType: 'DOUDIZHU', label: '🃏 斗地主 ✓', desc: '3人·已启用', disabled: false },
      { gameType: 'MAHJONG', label: '🀄 麻将', desc: '4人·即将开放', disabled: true },
    ],
    showJoinModal: false,
    joinRoomId: '',
    matching: false,
    pveLoading: false,
  },

  onShow() {
    const gameType = (storage.getSelectedGameType() || 'DOUDIZHU') as GameType
    this.setData({ gameType })
    this.refreshHeader()
  },

  async refreshHeader() {
    try {
      const summary = await loadProfileSummary()
      const nickname = summary.user?.nickname || '玩家'
      this.setData({
        nickname,
        avatarText: nickname.slice(0, 1),
        gold: summary.gold,
        tierText: summary.tierText,
      })
    } catch (err) {
      await handleApiError(err)
    }
  },

  onSelectGame(e: WechatMiniprogram.BaseEvent) {
    const gameType = e.currentTarget.dataset.gameType as GameType
    const meta = GAME_CATALOG[gameType]
    if (!meta || gameType === 'MAHJONG') {
      wx.showToast({ title: '麻将即将开放', icon: 'none' })
      return
    }
    storage.setSelectedGameType(gameType)
    this.setData({ gameType })
  },

  async startMatch(mode: MatchMode) {
    if (this.data.matching) return
    const { gameType } = this.data
    this.setData({ matching: true })
    wx.showLoading({ title: '匹配中…', mask: true })
    try {
      const res = await match.quick({ gameType, mode })
      wx.hideLoading()
      if (!navigateByMatchStatus(res, gameType)) {
        wx.showToast({ title: '匹配状态异常', icon: 'none' })
      }
    } catch (err) {
      if (err instanceof ApiError && err.code === 40005) {
        wx.hideLoading()
        const res = await match.status(gameType)
        navigateByMatchStatus(res, gameType)
        return
      }
      wx.hideLoading()
      await handleApiError(err)
    } finally {
      this.setData({ matching: false })
    }
  },

  onQuickMatch() {
    this.startMatch('MATCH')
  },

  onRanked() {
    this.startMatch('RANKED')
  },

  async onCreateRoom() {
    const { gameType } = this.data
    wx.showLoading({ title: '创建中…', mask: true })
    try {
      const detail = await room.create({
        gameType,
        mode: 'FRIEND',
        config: { baseScore: 1, enableGrab: true, enableDouble: true },
      })
      storage.setLastRoomId(detail.roomId)
      wx.hideLoading()
      wx.redirectTo({ url: `/pages/room/room?roomId=${encodeURIComponent(detail.roomId)}` })
    } catch (err) {
      wx.hideLoading()
      await handleApiError(err)
    }
  },

  onJoinRoom() {
    this.setData({ showJoinModal: true, joinRoomId: '' })
  },

  onJoinCancel() {
    this.setData({ showJoinModal: false, joinRoomId: '' })
  },

  onJoinInput(e: WechatMiniprogram.Input) {
    this.setData({ joinRoomId: (e.detail.value || '').trim() })
  },

  async onJoinConfirm() {
    const roomId = this.data.joinRoomId.trim()
    if (!roomId) {
      wx.showToast({ title: '请输入房间号', icon: 'none' })
      return
    }
    wx.showLoading({ title: '加入中…', mask: true })
    try {
      await room.join(roomId)
      storage.setLastRoomId(roomId)
      this.setData({ showJoinModal: false })
      wx.hideLoading()
      wx.redirectTo({ url: `/pages/room/room?roomId=${encodeURIComponent(roomId)}` })
    } catch (err) {
      wx.hideLoading()
      await handleApiError(err)
    }
  },

  goSign() {
    wx.navigateTo({ url: '/pages/sign/sign' })
  },

  goWallet() {
    wx.navigateTo({ url: '/pages/wallet/wallet' })
  },

  goRank() {
    wx.navigateTo({ url: `/pages/rank/rank?gameType=${this.data.gameType}` })
  },

  onGoRank() {
    this.goRank()
  },

  async onPvePractice() {
    if (this.data.pveLoading || this.data.matching) return
    this.setData({ pveLoading: true })
    wx.showLoading({ title: '开局中…', mask: true })
    try {
      await enterPveGame(this.data.gameType)
    } catch (err) {
      wx.hideLoading()
      await handleApiError(err)
    } finally {
      this.setData({ pveLoading: false })
    }
  },
})
