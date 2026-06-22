import { DEFAULT_GAME_TYPE, type GameType } from './config/game'
import type { PendingSettlement } from './types/api'
import { handlePageNotFound } from './utils/navigate'
import { buildViewportMeta, type ViewportMeta } from './utils/viewport'

export interface IAppOption {
  globalData: {
    selectedGameType: GameType
    pendingSettlement: PendingSettlement | null
    viewport: ViewportMeta | null
  }
  notifyViewportChange(): void
}

App<IAppOption>({
  globalData: {
    selectedGameType: DEFAULT_GAME_TYPE,
    pendingSettlement: null,
    viewport: null,
  },

  onLaunch() {
    this.globalData.viewport = buildViewportMeta()
    wx.onWindowResize(() => {
      this.notifyViewportChange()
    })
    wx.onPageNotFound((res) => {
      handlePageNotFound(res.path, res.query)
    })
  },

  onPageNotFound(res: { path: string; query: Record<string, string>; isEntryPage: boolean }) {
    handlePageNotFound(res.path, res.query)
  },

  notifyViewportChange() {
    this.globalData.viewport = buildViewportMeta()
    const pages = getCurrentPages()
    const current = pages[pages.length - 1] as WechatMiniprogram.Page.TrivialInstance & {
      refreshViewport?: () => void
    }
    current?.refreshViewport?.()
  },
})
