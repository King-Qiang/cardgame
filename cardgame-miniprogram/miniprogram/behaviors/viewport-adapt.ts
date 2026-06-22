import { buildViewportMeta } from '../utils/viewport'
import type { IAppOption } from '../app'

export default Behavior({
  data: {
    rootFontSize: '',
    viewportPageStyle: '',
  },

  lifetimes: {
    attached() {
      this.refreshViewport()
    },
  },

  pageLifetimes: {
    show() {
      this.refreshViewport()
    },
  },

  methods: {
    refreshViewport() {
      const meta = buildViewportMeta()
      this.setData({
        rootFontSize: meta.rootFontSize,
        viewportPageStyle: meta.pageStyle,
      })
      const app = getApp<IAppOption>()
      app.globalData.viewport = meta
    },
  },
})
