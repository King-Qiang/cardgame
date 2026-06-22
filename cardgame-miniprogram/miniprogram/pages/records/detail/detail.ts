import viewportAdapt from '../../../behaviors/viewport-adapt'
import subPageHeader from '../../../behaviors/sub-page-header'
import * as record from '../../../services/record'
import { handleApiError } from '../../../services/request'
import {
  formatDuration,
  formatGoldDelta,
  formatRecordTime,
  gameTypeLabel,
  modeLabel,
  shortRecordId,
} from '../../../utils/record-label'
import type { PlayerRecordParticipant } from '../../../types/api'

interface ParticipantRow extends PlayerRecordParticipant {
  goldText: string
  seatText: string
  tagText: string
}

Page({
  behaviors: [viewportAdapt, subPageHeader],
  data: {
    loading: true,
    recordId: '',
    title: '战绩详情',
    summaryLine: '',
    resultLine: '',
    isWin: false,
    participants: [] as ParticipantRow[],
  },

  onLoad(options: { recordId?: string }) {
    const recordId = options.recordId || ''
    this.setData({ recordId, title: shortRecordId(recordId) || '战绩详情' })
    this.loadDetail(recordId)
  },

  onBack() {
    wx.navigateBack({ fail: () => wx.reLaunch({ url: '/pages/records/records' }) })
  },

  async loadDetail(recordId: string) {
    if (!recordId) {
      this.setData({ loading: false })
      return
    }
    this.setData({ loading: true })
    try {
      const detail = await record.detail(recordId)
      const my = detail.mySettlement
      const summaryLine = [
        gameTypeLabel(detail.gameType),
        modeLabel(detail.mode),
        formatRecordTime(detail.endAt),
        formatDuration(detail.durationSec),
      ].join(' · ')
      const resultLine = `${my.isWin ? '胜利' : '失败'} ${formatGoldDelta(my.goldDelta)}`
      const participants = (detail.participants || []).map((p) => ({
        ...p,
        goldText: formatGoldDelta(p.goldDelta),
        seatText: p.seat != null ? String(p.seat) : '—',
        tagText: p.isLandlord ? '(地主)' : '',
      }))
      this.setData({ summaryLine, resultLine, isWin: my.isWin, participants, loading: false })
    } catch (err) {
      this.setData({ loading: false })
      await handleApiError(err)
    }
  },
})
