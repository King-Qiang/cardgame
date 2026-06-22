import viewportAdapt from '../../behaviors/viewport-adapt'
import subPageHeader from '../../behaviors/sub-page-header'
import * as shop from '../../services/shop'
import * as wallet from '../../services/wallet'
import { handleApiError, ApiError } from '../../services/request'
import { formatGold } from '../../utils/format'
import { shopItemGrantedGold } from '../../utils/transaction-label'
import type { ShopItem } from '../../types/api'

interface ShopItemView extends ShopItem {
  grantedGold: number
  priceText: string
}

Page({
  behaviors: [viewportAdapt, subPageHeader],
  data: {
    gold: -1,
    items: [] as ShopItemView[],
    loading: true,
    showBuyModal: false,
    buyItem: null as ShopItemView | null,
    buyConfirmText: '',
    buying: false,
  },

  onShow() {
    this.loadItems()
  },

  onBack() {
    wx.navigateBack({ fail: () => wx.reLaunch({ url: '/pages/profile/profile' }) })
  },

  goRecharge() {
    wx.navigateTo({ url: '/pages/recharge/recharge' })
  },

  async loadItems() {
    this.setData({ loading: true })
    try {
      const list = await shop.listItems()
      const items = list.map((item) => ({
        ...item,
        grantedGold: shopItemGrantedGold(item),
        priceText: formatGold(item.price),
      }))
      this.setData({ items, loading: false })
    } catch (err) {
      this.setData({ loading: false })
      await handleApiError(err)
    }
  },

  onBuyTap(e: WechatMiniprogram.TouchEvent) {
    const id = e.currentTarget.dataset.id as number
    const buyItem = this.data.items.find((i) => i.id === id)
    if (!buyItem) return
    const after = this.data.gold >= 0 ? (this.data.gold as number) - buyItem.price : 0
    this.setData({
      showBuyModal: true,
      buyItem,
      buyConfirmText: `${buyItem.name}\n花费 ${buyItem.priceText} 金币\n购买后余额约 ${formatGold(Math.max(0, after))}`,
    })
  },

  onBuyCancel() {
    this.setData({ showBuyModal: false, buyItem: null })
  },

  async onBuyConfirm() {
    const { buyItem } = this.data
    if (!buyItem || this.data.buying) return
    this.setData({ buying: true })
    try {
      const res = await shop.buy(buyItem.id)
      this.setData({ showBuyModal: false, buyItem: null })
      wx.showToast({ title: `+${res.grantedGold} 金币`, icon: 'success' })
      const balance = await wallet.balance()
      this.setData({ gold: balance.gold ?? 0 })
      await this.loadItems()
    } catch (err) {
      if (err instanceof ApiError && err.code === 60001) {
        wx.showModal({
          title: '金币不足',
          content: err.message,
          cancelText: '取消',
          confirmText: '去充值',
          success: (res) => {
            if (res.confirm) this.goRecharge()
          },
        })
      } else {
        await handleApiError(err)
      }
    } finally {
      this.setData({ buying: false })
    }
  },
})
