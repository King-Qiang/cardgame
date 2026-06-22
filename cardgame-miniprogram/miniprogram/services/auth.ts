import type { LoginResponse, UserInfo } from '../types/api'
import * as request from './request'
import * as user from './user'
import * as storage from '../utils/storage'

export async function loginWithWechat(): Promise<UserInfo> {
  const loginResult = await wx.login()
  if (!loginResult.code) {
    throw new Error('微信登录失败')
  }
  const data = await request.post<LoginResponse>(
    '/auth/wechat/login',
    { code: loginResult.code },
    { auth: false },
  )
  storage.setTokens(data.accessToken, data.refreshToken)
  storage.setUserInfo(data.user)
  return data.user
}

export function isLoggedIn(): boolean {
  return !!storage.getAccessToken()
}

/** 本地有 token 但 user_info 缺失时，从 /user/me 补全（结算页匹配 userId 依赖此项） */
export async function ensureUserInfo(gameType = 'DOUDIZHU'): Promise<UserInfo | null> {
  const cached = storage.getUserInfo<UserInfo>()
  if (cached?.id) {
    return cached
  }
  if (!isLoggedIn()) {
    return null
  }
  const me = await user.me(gameType)
  const info: UserInfo = { id: me.id, nickname: me.nickname, avatar: me.avatar }
  storage.setUserInfo(info)
  return info
}

export function logout(): void {
  storage.clearAuth()
}
