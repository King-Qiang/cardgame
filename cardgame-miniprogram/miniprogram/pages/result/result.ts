import viewportAdapt from '../../behaviors/viewport-adapt'
import type { IAppOption } from '../../app'
import type { GameType } from '../../config/game'
import { ensureUserInfo } from '../../services/auth'
import * as rank from '../../services/rank'
import { tierLabel } from '../../utils/format'
import type { PendingSettlement } from '../../types/api'
import {
  findMySettlement,
  formatSettlementGold,
  resolveIsWin,
} from '../../utils/settlement-view'

Page({
  behaviors: [viewportAdapt],
  data: {
    loaded: false,
    isWin: false,
    title: '对局结束',
    goldDelta: 0,
    goldText: '0金',
    scoreDelta: 0,
    multiplier: 1,
    tierText: '',
    rankStats: '',
    mode: 'FRIEND',
    gameType: 'DOUDIZHU',
  },

  onLoad() {
    const app = getApp<IAppOption>()
    const pending = app.globalData.pendingSettlement
    if (!pending) {
      wx.reLaunch({ url: '/pages/lobby/lobby' })
      return
    }
    this.renderSettlement(pending)
  },

  onUnload() {
    const app = getApp<IAppOption>()
    app.globalData.pendingSettlement = null
  },

  async renderSettlement(pending: PendingSettlement) {
    const userInfo = await ensureUserInfo(pending.gameType)
    const me = findMySettlement(pending.settlements, userInfo?.id)
    const isWin = resolveIsWin(me, pending.result.winnerSeat)
    const goldDelta = me?.goldDelta ?? 0
    const scoreDelta = me?.scoreDelta ?? 0

    let tierText = ''
    let rankStats = ''
    if (pending.mode === 'RANKED') {
      try {
        const info = await rank.me(pending.gameType as GameType)
        tierText = `${tierLabel(info.tier)} ${info.points}分`
        rankStats = `${info.wins}胜 ${info.losses}负`
      } catch {
        tierText = '段位更新中…'
      }
    }

    this.setData({
      loaded: true,
      isWin,
      title: isWin ? '胜利！' : '失败',
      goldDelta,
      goldText: formatSettlementGold(goldDelta),
      scoreDelta,
      multiplier: pending.result.multiplier || 1,
      tierText,
      rankStats,
      mode: pending.mode,
      gameType: pending.gameType,
    })
  },

  onRematch() {
    wx.reLaunch({
      url: `/pages/match/match?gameType=${encodeURIComponent(this.data.gameType)}&mode=${encodeURIComponent(this.data.mode === 'RANKED' ? 'RANKED' : 'MATCH')}`,
    })
  },

  onGoLobby() {
    wx.reLaunch({ url: '/pages/lobby/lobby' })
  },
})
