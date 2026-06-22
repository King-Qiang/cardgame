import viewportAdapt from '../../behaviors/viewport-adapt'
import { ensureUserInfo, loginWithWechat, isLoggedIn } from '../../services/auth'
import { handleApiError, ApiError } from '../../services/request'
import { navigatePostLoginRoute, resolvePostLoginRoute } from '../../utils/post-login-route'
import { safeRedirectTo } from '../../utils/navigate'
import * as room from '../../services/room'
import * as storage from '../../utils/storage'

Page({
  behaviors: [viewportAdapt],
  data: {
    statusText: '正在登录…',
    showRetry: false,
  },

  onLoad(options: Record<string, string | undefined>) {
    const inviteRoomId = (options.roomId || '').trim()
    if (inviteRoomId) {
      storage.setPendingRoomInvite(inviteRoomId)
    }
    this.bootstrap()
  },

  async bootstrap() {
    this.setData({ statusText: '正在登录…', showRetry: false })
    try {
      if (!isLoggedIn()) {
        await loginWithWechat()
      } else {
        await ensureUserInfo()
      }
      await this.afterLogin()
    } catch (err) {
      if (err instanceof ApiError && err.code === 30002) {
        await handleApiError(err)
        this.setData({ statusText: '账号受限', showRetry: true })
        return
      }
      await handleApiError(err)
      this.setData({ statusText: '登录失败，请重试', showRetry: true })
    }
  },

  async afterLogin() {
    const inviteRoomId = storage.getPendingRoomInvite()
    if (inviteRoomId) {
      storage.clearPendingRoomInvite()
      try {
        await room.join(inviteRoomId)
        storage.setLastRoomId(inviteRoomId)
        safeRedirectTo(`/pages/room/room?roomId=${encodeURIComponent(inviteRoomId)}`)
        return
      } catch (err) {
        if (!(err instanceof ApiError)) {
          await handleApiError(err)
        } else {
          wx.showToast({ title: err.message, icon: 'none' })
        }
      }
    }

    const route = await resolvePostLoginRoute()
    navigatePostLoginRoute(route)
  },

  onRetry() {
    this.bootstrap()
  },
})
