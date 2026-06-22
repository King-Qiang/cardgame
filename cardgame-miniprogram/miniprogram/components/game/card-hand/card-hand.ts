import type { IAppOption } from '../../../app'
import { fitHandLayout, resolveHandAvailWidthRpx } from '../../../utils/hand-layout'

Component({
  properties: {
    cards: { type: Array, value: [] as string[] },
    selected: { type: Array, value: [] as string[] },
    /** 视口变化时触发重新测量（来自 page viewportPageStyle） */
    viewportKey: { type: String, value: '' },
  },
  data: {
    items: [] as { code: string; isSelected: boolean }[],
    cardW: 64,
    cardH: 90,
    overlap: 12,
    rowWidth: 700,
    handPaddingTop: 12,
  },
  observers: {
    'cards, selected'(cards: string[], selected: string[]) {
      const set = new Set(selected || [])
      this.setData({
        items: (cards || []).map((code) => ({ code, isSelected: set.has(code) })),
      })
      this.updateLayout((cards || []).length)
    },
    viewportKey() {
      this.updateLayout(((this.properties.cards as string[]) || []).length)
    },
  },
  lifetimes: {
    attached() {
      this.updateLayout(((this.properties.cards as string[]) || []).length)
    },
  },
  pageLifetimes: {
    show() {
      this.updateLayout(((this.properties.cards as string[]) || []).length)
    },
  },
  methods: {
    updateLayout(count: number) {
      const app = getApp<IAppOption>()
      const uiScale = app.globalData.viewport?.metrics.uiScale ?? 1
      const availWidth = resolveHandAvailWidthRpx()
      const layout = fitHandLayout(count, availWidth, uiScale)
      this.setData({
        ...layout,
        handPaddingTop: Math.round(10 * uiScale),
      })
    },
    onTap(e: WechatMiniprogram.TouchEvent) {
      const code = e.currentTarget.dataset.code as string
      this.triggerEvent('toggle', { code })
    },
  },
})
