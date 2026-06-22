const ACCESS_TOKEN_KEY = 'access_token'
const REFRESH_TOKEN_KEY = 'refresh_token'
const USER_INFO_KEY = 'user_info'
const LAST_ROOM_ID_KEY = 'last_room_id'
const SELECTED_GAME_TYPE_KEY = 'selected_game_type'
const PENDING_ROOM_INVITE_KEY = 'pending_room_invite'

export function getAccessToken(): string {
  return wx.getStorageSync(ACCESS_TOKEN_KEY) || ''
}

export function getRefreshToken(): string {
  return wx.getStorageSync(REFRESH_TOKEN_KEY) || ''
}

export function setTokens(accessToken: string, refreshToken: string): void {
  wx.setStorageSync(ACCESS_TOKEN_KEY, accessToken)
  wx.setStorageSync(REFRESH_TOKEN_KEY, refreshToken)
}

export function clearAuth(): void {
  wx.removeStorageSync(ACCESS_TOKEN_KEY)
  wx.removeStorageSync(REFRESH_TOKEN_KEY)
  wx.removeStorageSync(USER_INFO_KEY)
}

export function getUserInfo<T>(): T | null {
  const raw = wx.getStorageSync(USER_INFO_KEY)
  return raw || null
}

export function setUserInfo(user: unknown): void {
  wx.setStorageSync(USER_INFO_KEY, user)
}

export function getLastRoomId(): string {
  const raw = wx.getStorageSync(LAST_ROOM_ID_KEY)
  return typeof raw === 'string' ? raw.trim() : ''
}

export function setLastRoomId(roomId: string): void {
  wx.setStorageSync(LAST_ROOM_ID_KEY, roomId)
}

export function clearLastRoomId(): void {
  wx.removeStorageSync(LAST_ROOM_ID_KEY)
}

export function getSelectedGameType(): string {
  return wx.getStorageSync(SELECTED_GAME_TYPE_KEY) || 'DOUDIZHU'
}

export function setSelectedGameType(gameType: string): void {
  wx.setStorageSync(SELECTED_GAME_TYPE_KEY, gameType)
}

export function getPendingRoomInvite(): string {
  const raw = wx.getStorageSync(PENDING_ROOM_INVITE_KEY)
  return typeof raw === 'string' ? raw.trim() : ''
}

export function setPendingRoomInvite(roomId: string): void {
  wx.setStorageSync(PENDING_ROOM_INVITE_KEY, roomId)
}

export function clearPendingRoomInvite(): void {
  wx.removeStorageSync(PENDING_ROOM_INVITE_KEY)
}
