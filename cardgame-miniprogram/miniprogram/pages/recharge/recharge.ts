import viewportAdapt from '../../behaviors/viewport-adapt'
import subPageHeader from '../../behaviors/sub-page-header'
import * as order from '../../services/order'
import * as wallet from '../../services/wallet'
import { handleApiError } from '../../services/request'
import { formatGold } from '../../utils/format'
import { RECHARGE_TIERS } from '../../utils/transaction-label'

Page({
  behaviors: [viewportAdapt, subPageHeader],
  data: {
    tiers: RECHARGE_TIERS,
    selectedIndex: 0,
    pendingOrderNo: '',
    paying: false,
    statusText: '选择充值档位后创建订单',
  },

  onBack() {
    wx.navigateBack({ fail: () => wx.reLaunch({ url: '/pages/shop/shop' }) })
  },

  onSelectTier(e: WechatMiniprogram.TouchEvent) {
    const index = Number(e.currentTarget.dataset.index)
    this.setData({ selectedIndex: index, pendingOrderNo: '', statusText: '点击创建订单' })
  },

  async onCreateOrder() {
    if (this.data.paying) return
    const tier = this.data.tiers[this.data.selectedIndex]
    if (!tier) return
    this.setData({ paying: true, statusText: '创建订单中…' })
    try {
      const res = await order.createOrder({
        amount: tier.amount,
        goldAmount: tier.goldAmount,
        payChannel: 'WECHAT',
      })
      this.setData({
        pendingOrderNo: res.orderNo,
        statusText: `订单 ${res.orderNo} · 待支付`,
        paying: false,
      })
    } catch (err) {
      this.setData({ paying: false })
      await handleApiError(err)
    }
  },

  async onMockPay() {
    const { pendingOrderNo } = this.data
    if (!pendingOrderNo || this.data.paying) return
    this.setData({ paying: true, statusText: '模拟支付中…' })
    try {
      const res = await order.payCallback(pendingOrderNo)
      wx.showToast({ title: `+${res.goldAmount} 金币`, icon: 'success' })
      const balance = await wallet.balance()
      this.setData({ gold: balance.gold ?? 0 })
      this.setData({
        paying: false,
        pendingOrderNo: '',
        statusText: '充值成功',
      })
    } catch (err) {
      this.setData({ paying: false })
      await handleApiError(err)
    }
  },
})
