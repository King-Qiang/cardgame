import viewportAdapt from '../../behaviors/viewport-adapt'
import subPageHeader from '../../behaviors/sub-page-header'

Page({
  behaviors: [viewportAdapt, subPageHeader],
  onBack() {
    wx.navigateBack({ fail: () => wx.reLaunch({ url: '/pages/profile/profile' }) })
  },
})
