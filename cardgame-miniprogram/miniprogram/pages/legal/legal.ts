import viewportAdapt from '../../behaviors/viewport-adapt'
import subPageHeader from '../../behaviors/sub-page-header'

Page({
  behaviors: [viewportAdapt, subPageHeader],
  data: { title: '用户协议' },
  onLoad(query: Record<string, string | undefined>) {
    this.setData({ title: query.type === 'privacy' ? '隐私政策' : '用户协议' })
  },
  onBack() {
    wx.navigateBack({ fail: () => wx.reLaunch({ url: '/pages/profile/profile' }) })
  },
})
