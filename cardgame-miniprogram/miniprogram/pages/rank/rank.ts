import viewportAdapt from '../../behaviors/viewport-adapt'
import subPageHeader from '../../behaviors/sub-page-header'
import type { GameType } from '../../config/game'
import * as rank from '../../services/rank'
import { handleApiError } from '../../services/request'
import { tierLabel } from '../../utils/format'
import type { RankLeaderboardItem } from '../../types/api'

interface LeaderRow extends RankLeaderboardItem {
  tierText: string
  recordText: string
}

Page({
  behaviors: [viewportAdapt, subPageHeader],
  data: {
    gameType: 'DOUDIZHU' as GameType,
    seasonId: '',
    myTierText: '—',
    myProgressText: '',
    list: [] as LeaderRow[],
    loading: true,
  },

  onLoad(query: Record<string, string | undefined>) {
    const gameType = (query.gameType || 'DOUDIZHU') as GameType
    this.setData({ gameType })
    this.refresh()
  },

  onShow() {
    if (!this.data.loading) {
      this.refresh()
    }
  },

  onTab(e: WechatMiniprogram.TouchEvent) {
    const gameType = e.currentTarget.dataset.gameType as GameType
    if (gameType === 'MAHJONG') {
      wx.showToast({ title: '麻将即将开放', icon: 'none' })
      return
    }
    if (gameType === this.data.gameType) return
    this.setData({ gameType })
    this.refresh()
  },

  async refresh() {
    this.setData({ loading: true })
    const { gameType } = this.data
    try {
      const [myRank, board] = await Promise.all([
        rank.me(gameType),
        rank.leaderboard(gameType, 1, 20),
      ])
      const myTierText = `${tierLabel(myRank.tier)} · ${myRank.points}分`
      let myProgressText = `${myRank.wins}胜 ${myRank.losses}负`
      if (myRank.pointsToNextTier != null && myRank.nextTier) {
        myProgressText += ` · 距${tierLabel(myRank.nextTier)} ${myRank.pointsToNextTier}分`
      }
      const list = board.list.map((row) => ({
        ...row,
        tierText: tierLabel(row.tier),
        recordText: `${row.wins}/${row.losses}`,
      }))
      this.setData({
        loading: false,
        seasonId: myRank.seasonId,
        myTierText,
        myProgressText,
        list,
      })
    } catch (err) {
      this.setData({ loading: false })
      await handleApiError(err)
    }
  },
})
