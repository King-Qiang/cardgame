/** 横屏 UI 设计基准高度（rpx），见 DESIGN.md §7 */
const DESIGN_HEIGHT_RPX = 720

const MIN_SCALE = 0.58
const MAX_SCALE = 1

export type ViewportTier = 'xl' | 'lg' | 'md' | 'sm' | 'xs'

export interface ViewportMetrics {
  windowWidth: number
  windowHeight: number
  heightRpx: number
  uiScale: number
  tier: ViewportTier
  model: string
  platform: string
}

export interface ViewportMeta {
  rootFontSize: string
  pageStyle: string
  metrics: ViewportMetrics
}

function tierOf(scale: number): ViewportTier {
  if (scale >= 0.95) return 'xl'
  if (scale >= 0.82) return 'lg'
  if (scale >= 0.72) return 'md'
  if (scale >= 0.64) return 'sm'
  return 'xs'
}

/** 已知窄屏机型微调（在视口比例基础上） */
function modelScaleAdjust(model: string, heightRpx: number): number {
  const name = model.toLowerCase()
  if (name.includes('iphone se') || name.includes('iphone12,8') || name.includes('iphone14,6')) {
    return 0.94
  }
  if (heightRpx < 340) return 0.92
  if (heightRpx < 380) return 0.96
  return 1
}

export function buildViewportMeta(): ViewportMeta {
  const win = wx.getWindowInfo()
  const device = wx.getDeviceInfo()
  const { windowWidth: w, windowHeight: h } = win

  const heightRpx = (750 * h) / w
  let uiScale = heightRpx / DESIGN_HEIGHT_RPX
  uiScale *= modelScaleAdjust(device.model || '', heightRpx)
  uiScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, uiScale))

  const defaultRootPx = w / 750
  const rootFontSizePx = defaultRootPx * uiScale

  const pageStyle = [
    `--ui-scale:${uiScale.toFixed(3)}`,
    `--vh-rpx:${heightRpx.toFixed(1)}`,
    `--gap-md:${Math.round(12 * uiScale)}rpx`,
    `--gap-sm:${Math.round(8 * uiScale)}rpx`,
    `--panel-radius:${Math.max(8, Math.round(12 * uiScale))}rpx`,
  ].join(';')

  return {
    rootFontSize: `${rootFontSizePx.toFixed(4)}px`,
    pageStyle,
    metrics: {
      windowWidth: w,
      windowHeight: h,
      heightRpx,
      uiScale,
      tier: tierOf(uiScale),
      model: device.model || '',
      platform: device.platform || '',
    },
  }
}
