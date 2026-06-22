/** 计算手牌行在可用宽度内的一行展示参数（保证 rowWidth <= availWidthRpx） */

export interface HandLayout {
  cardW: number
  cardH: number
  overlap: number
  rowWidth: number
}

export function measureHandRowWidth(count: number, cardW: number, step: number): number {
  if (count <= 1) return cardW
  return cardW + (count - 1) * step
}

export function resolveHandAvailWidthRpx(): number {
  const win = wx.getWindowInfo()
  const pxToRpx = 750 / win.windowWidth
  const contentPx = win.safeArea?.width ?? win.windowWidth
  // page-full 已吃掉 safe-area，再留少量内边距
  return Math.floor(contentPx * pxToRpx - 32)
}

export function fitHandLayout(count: number, availWidthRpx: number, uiScale: number): HandLayout {
  const n = Math.max(1, count)
  const minCardW = Math.max(30, Math.round(32 * uiScale))
  const maxCardW = Math.round(64 * uiScale)
  const minStep = Math.max(2, Math.round(3 * uiScale))

  let cardW = maxCardW
  let overlap = Math.round(10 * uiScale)

  const stepOf = (w: number, o: number) => (n <= 1 ? w : Math.max(minStep, w - o))
  const widthOf = (w: number, o: number) => measureHandRowWidth(n, w, stepOf(w, o))

  let guard = 0
  while (n > 1 && widthOf(cardW, overlap) > availWidthRpx && guard < 200) {
    guard += 1
    const step = stepOf(cardW, overlap)
    if (step > minStep) {
      overlap += 2
    } else if (cardW > minCardW) {
      cardW -= 2
      overlap = Math.round(8 * uiScale)
    } else {
      break
    }
  }

  while (n > 1 && widthOf(cardW, overlap) > availWidthRpx && cardW > minCardW) {
    cardW -= 1
    overlap = Math.min(overlap, cardW - minStep)
  }

  const step = stepOf(cardW, overlap)
  const overlapFinal = cardW - step

  return {
    cardW,
    cardH: Math.round(cardW * 1.4),
    overlap: overlapFinal,
    rowWidth: widthOf(cardW, overlapFinal),
  }
}
