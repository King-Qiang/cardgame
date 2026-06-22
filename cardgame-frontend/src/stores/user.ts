import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as authApi from '../api/auth'
import {
  clearAuth,
  getPermissions,
  getStoredUser,
  getToken,
  setPermissions,
  setRefreshToken,
  setStoredUser,
  setToken,
} from '../utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const user = ref(getStoredUser())
  const permissions = ref<string[]>([])

  async function login(username: string, password: string) {
    const data = await authApi.login({ username, password })
    token.value = data.accessToken
    user.value = data.user
    permissions.value = data.permissions
    setToken(data.accessToken)
    setRefreshToken(data.refreshToken)
    setStoredUser(data.user)
    setPermissions(data.permissions)
    return data
  }

  async function logout() {
    try {
      await authApi.logout()
    } catch {
      // 忽略登出接口错误，本地仍清除登录态
    }
    reset()
  }

  function reset() {
    token.value = null
    user.value = null
    permissions.value = []
    clearAuth()
  }

  function restore() {
    token.value = getToken()
    user.value = getStoredUser()
    permissions.value = getPermissions()
  }

  return { token, user, permissions, login, logout, reset, restore }
})
