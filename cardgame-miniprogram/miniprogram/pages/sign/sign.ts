import viewportAdapt from '../../behaviors/viewport-adapt'
import subPageHeader from '../../behaviors/sub-page-header'
import * as activity from '../../services/activity'
import * as wallet from '../../services/wallet'
import { handleApiError } from '../../services/request'
import { buildSignCells } from '../../utils/transaction-label'

Page({
  behaviors: [viewportAdapt, subPageHeader],
  data: {
    loading: true,
    signedToday: false,
    streakDay: 0,
    headerText: '',
    actionText: '立即签到',
    actionDisabled: false,
    signing: false,
    cells: [] as { day: number; reward: number; signed: boolean; today: boolean }[],
  },

  onShow() {
    this.loadStatus()
  },

  async loadStatus() {
    this.setData({ loading: true })
    try {
      const status = await activity.dailySignStatus()
      const cells = buildSignCells(status)
      const headerText = status.signedToday
        ? `连续 ${status.streakDay} 天 · 今日已领 +${status.rewardGold} 金`
        : `连续签到 · 今日可领 +${status.rewardGold} 金`
      this.setData({
        loading: false,
        signedToday: status.signedToday,
        streakDay: status.streakDay,
        headerText,
        actionText: status.signedToday ? `明日可领 +${status.nextRewardGold}` : '立即签到',
        actionDisabled: status.signedToday,
        cells,
      })
    } catch (err) {
      this.setData({ loading: false })
      await handleApiError(err)
    }
  },

  async onSignTap() {
    if (this.data.actionDisabled || this.data.signing) return
    this.setData({ signing: true })
    try {
      const status = await activity.dailySign()
      wx.showToast({ title: `+${status.rewardGold} 金币`, icon: 'success' })
      const balance = await wallet.balance()
      this.setData({ gold: balance.gold ?? 0 })
      await this.loadStatus()
    } catch (err) {
      await handleApiError(err)
    } finally {
      this.setData({ signing: false })
    }
  },
})
