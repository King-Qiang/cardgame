import * as wallet from '../services/wallet'

export default Behavior({
  data: {
    gold: -1,
  },
  pageLifetimes: {
    show() {
      this.refreshGold()
    },
  },
  methods: {
    async refreshGold() {
      try {
        const balance = await wallet.balance()
        this.setData({ gold: balance.gold ?? 0 })
      } catch {
        this.setData({ gold: -1 })
      }
    },
    onBack() {
      wx.navigateBack({ fail: () => wx.reLaunch({ url: '/pages/lobby/lobby' }) })
    },
  },
})
