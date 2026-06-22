import { parseCard } from '../../../utils/cards'

Component({
  properties: {
    code: { type: String, value: '' },
    selected: { type: Boolean, value: false },
    faceDown: { type: Boolean, value: false },
    compact: { type: Boolean, value: false },
    /** 牌桌中央对手出牌：固定尺寸牌背 */
    playBack: { type: Boolean, value: false },
    /** 对手头像旁桌面出牌 */
    playTable: { type: Boolean, value: false },
    /** 自定义宽高（rpx），用于手牌区自适应 */
    customWidth: { type: Number, value: 0 },
    customHeight: { type: Number, value: 0 },
  },
  data: {
    rank: '',
    suitSymbol: '',
    isRed: false,
    isJoker: false,
    sizeStyle: '',
  },
  observers: {
    code(code: string) {
      if (!code || this.properties.faceDown) {
        this.setData({ rank: '', suitSymbol: '', isRed: false, isJoker: false })
        return
      }
      const view = parseCard(code)
      this.setData({
        rank: view.rank,
        suitSymbol: view.suitSymbol,
        isRed: view.isRed,
        isJoker: view.isJoker,
      })
    },
    'customWidth, customHeight'(w: number, h: number) {
      if (w > 0 && h > 0) {
        this.setData({ sizeStyle: `width:${w}rpx;height:${h}rpx;` })
      } else {
        this.setData({ sizeStyle: '' })
      }
    },
  },
})
