import viewportAdapt from '../../behaviors/viewport-adapt'
import * as record from '../../services/record'
import { handleApiError } from '../../services/request'
import {
  formatGoldDelta,
  formatRecordTime,
  gameTypeLabel,
  modeLabel,
  shortRecordId,
} from '../../utils/record-label'
import type { PlayerRecordListItem } from '../../types/api'

interface RecordRow extends PlayerRecordListItem {
  shortId: string
  gameTypeText: string
  modeText: string
  resultText: string
  goldText: string
  timeText: string
}

Page({
  behaviors: [viewportAdapt],
  data: {
    loading: true,
    empty: false,
    rows: [] as RecordRow[],
    gameType: 'DOUDIZHU',
  },

  onShow() {
    this.loadRecords()
  },

  async loadRecords() {
    this.setData({ loading: true })
    try {
      const res = await record.list({ page: 1, pageSize: 20, gameType: this.data.gameType })
      const rows = (res.list || []).map((item) => this.toRow(item))
      this.setData({ rows, empty: rows.length === 0, loading: false })
    } catch (err) {
      this.setData({ loading: false })
      await handleApiError(err)
    }
  },

  toRow(item: PlayerRecordListItem): RecordRow {
    return {
      ...item,
      shortId: shortRecordId(item.recordId),
      gameTypeText: gameTypeLabel(item.gameType),
      modeText: modeLabel(item.mode),
      resultText: item.isWin ? '胜利' : '失败',
      goldText: formatGoldDelta(item.myGoldDelta),
      timeText: formatRecordTime(item.endAt),
    }
  },

  onRowTap(e: WechatMiniprogram.TouchEvent) {
    const recordId = e.currentTarget.dataset.id as string
    if (!recordId) return
    wx.navigateTo({ url: `/pages/records/detail/detail?recordId=${recordId}` })
  },

  goLobby() {
    wx.reLaunch({ url: '/pages/lobby/lobby' })
  },
})
