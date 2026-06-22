import { DOCK_TABS, type DockTab } from '../../../config/game'

import { safeReLaunch } from '../../../utils/navigate'

Component({
  properties: {
    active: {
      type: String,
      value: 'lobby' as DockTab,
    },
  },
  data: {
    tabs: DOCK_TABS,
  },
  methods: {
    onTap(e: WechatMiniprogram.TouchEvent) {
      const path = e.currentTarget.dataset.path as string
      const id = e.currentTarget.dataset.id as DockTab
      if (!path || id === this.properties.active) {
        return
      }
      safeReLaunch(path)
    },
  },
})
