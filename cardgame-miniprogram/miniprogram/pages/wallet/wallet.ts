import viewportAdapt from '../../behaviors/viewport-adapt'
import subPageHeader from '../../behaviors/sub-page-header'
import * as wallet from '../../services/wallet'
import { handleApiError } from '../../services/request'
import { formatGold } from '../../utils/format'
import { formatDateTime, transactionTypeLabel } from '../../utils/transaction-label'
import type { WalletTransaction } from '../../types/api'

interface TxView extends WalletTransaction {
  typeLabel: string
  timeText: string
  amountText: string
  balanceText: string
}

Page({
  behaviors: [viewportAdapt, subPageHeader],
  data: {
    rows: [] as TxView[],
    loading: true,
    empty: false,
  },

  onShow() {
    this.loadTransactions()
  },

  onBack() {
    wx.navigateBack({ fail: () => wx.reLaunch({ url: '/pages/profile/profile' }) })
  },

  async loadTransactions() {
    this.setData({ loading: true })
    try {
      const res = await wallet.transactions(1, 30)
      const rows = res.list.map((tx) => ({
        ...tx,
        typeLabel: transactionTypeLabel(tx.type),
        timeText: formatDateTime(tx.createdAt),
        amountText: `${tx.amount >= 0 ? '+' : ''}${formatGold(tx.amount)}`,
        balanceText: formatGold(tx.balanceAfter),
      }))
      this.setData({ rows, loading: false, empty: rows.length === 0 })
    } catch (err) {
      this.setData({ loading: false })
      await handleApiError(err)
    }
  },
})
