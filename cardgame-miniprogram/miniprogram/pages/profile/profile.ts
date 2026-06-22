import viewportAdapt from '../../behaviors/viewport-adapt'
import { loadProfileSummary } from '../../utils/profile-summary'
import { handleApiError } from '../../services/request'
import { logout } from '../../services/auth'

Page({
  behaviors: [viewportAdapt],
  data: {
    nickname: '玩家',
    avatarText: '?',
    userId: '—',
    gold: 0,
    tierText: '—',
    rankStats: '',
  },

  onShow() {
    this.refresh()
  },

  async refresh() {
    try {
      const summary = await loadProfileSummary()
      const nickname = summary.user?.nickname || '玩家'
      this.setData({
        nickname,
        avatarText: nickname.slice(0, 1),
        userId: summary.user?.id ? String(summary.user.id) : '—',
        gold: summary.gold,
        tierText: summary.tierText,
        rankStats: summary.rankStats,
      })
    } catch (err) {
      await handleApiError(err)
    }
  },

  goShop() {
    wx.navigateTo({ url: '/pages/shop/shop' })
  },

  goWallet() {
    wx.navigateTo({ url: '/pages/wallet/wallet' })
  },

  goSign() {
    wx.navigateTo({ url: '/pages/sign/sign' })
  },

  goRank() {
    wx.navigateTo({ url: '/pages/rank/rank' })
  },

  goSettings() {
    wx.navigateTo({ url: '/pages/settings/settings' })
  },

  goLegal(e: WechatMiniprogram.TouchEvent) {
    const type = e.currentTarget.dataset.type || 'user'
    wx.navigateTo({ url: `/pages/legal/legal?type=${type}` })
  },

  onLogout() {
    logout()
    wx.reLaunch({ url: '/pages/index/index' })
  },
})
