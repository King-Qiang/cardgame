import { ENV } from '../config/env'
import type { ApiResponse, TokenPairResponse } from '../types/api'
import * as storage from '../utils/storage'

export class ApiError extends Error {
  constructor(
    public code: number,
    message: string,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE'

interface RequestOptions {
  auth?: boolean
  query?: Record<string, string | number | boolean | undefined>
}

let refreshPromise: Promise<void> | null = null

function buildUrl(path: string, query?: RequestOptions['query']): string {
  const base = ENV.apiBase.replace(/\/$/, '')
  const normalized = path.startsWith('/') ? path : `/${path}`
  let url = `${base}${normalized}`
  if (query) {
    const params = Object.entries(query)
      .filter(([, v]) => v !== undefined && v !== '')
      .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
      .join('&')
    if (params) {
      url += `?${params}`
    }
  }
  return url
}

function requestRaw<T>(
  method: HttpMethod,
  path: string,
  data?: unknown,
  options: RequestOptions = {},
): Promise<T> {
  const { auth = true, query } = options
  const header: Record<string, string> = {
    'Content-Type': 'application/json',
  }
  if (auth) {
    const token = storage.getAccessToken()
    if (token) {
      header.Authorization = `Bearer ${token}`
    }
  }

  return new Promise((resolve, reject) => {
    wx.request({
      url: buildUrl(path, query),
      method,
      data: data as WechatMiniprogram.IAnyObject | undefined,
      header,
      success: (res) => {
        const body = res.data as ApiResponse<T>
        if (!body || typeof body.code !== 'number') {
          reject(new Error('响应格式错误'))
          return
        }
        if (body.code === 0) {
          resolve(body.data)
          return
        }
        reject(new ApiError(body.code, body.message || '请求失败'))
      },
      fail: (err) => reject(new Error(err.errMsg || '网络错误')),
    })
  })
}

async function refreshTokens(): Promise<void> {
  if (!refreshPromise) {
    refreshPromise = (async () => {
      const refreshToken = storage.getRefreshToken()
      if (!refreshToken) {
        throw new ApiError(10002, '未登录')
      }
      const data = await requestRaw<TokenPairResponse>(
        'POST',
        '/auth/refresh',
        { refreshToken },
        { auth: false },
      )
      storage.setTokens(data.accessToken, data.refreshToken)
    })().finally(() => {
      refreshPromise = null
    })
  }
  await refreshPromise
}

async function requestWithRetry<T>(
  method: HttpMethod,
  path: string,
  data?: unknown,
  options: RequestOptions = {},
): Promise<T> {
  try {
    return await requestRaw<T>(method, path, data, options)
  } catch (err) {
    if (err instanceof ApiError && err.code === 10002 && options.auth !== false) {
      await refreshTokens()
      return requestRaw<T>(method, path, data, options)
    }
    throw err
  }
}

export function get<T>(path: string, query?: RequestOptions['query']): Promise<T> {
  return requestWithRetry<T>('GET', path, undefined, { query })
}

export function post<T>(path: string, data?: unknown, options?: RequestOptions): Promise<T> {
  return requestWithRetry<T>('POST', path, data, options)
}

export function put<T>(path: string, data?: unknown, options?: RequestOptions): Promise<T> {
  return requestWithRetry<T>('PUT', path, data, options)
}

export function del<T>(path: string, query?: RequestOptions['query']): Promise<T> {
  return requestWithRetry<T>('DELETE', path, undefined, { query })
}

export async function handleApiError(err: unknown): Promise<boolean> {
  if (err instanceof ApiError) {
    if (err.code === 30002) {
      storage.clearAuth()
      wx.showModal({
        title: '账号受限',
        content: err.message,
        showCancel: false,
        success: () => wx.reLaunch({ url: '/pages/index/index' }),
      })
      return true
    }
    wx.showToast({ title: err.message, icon: 'none' })
    return true
  }
  if (err instanceof Error) {
    const msg = err.message || ''
    if (/ERR_ADDRESS_UNREACHABLE|request:fail|timeout/i.test(msg)) {
      wx.showModal({
        title: '无法连接服务器',
        content: `请确认：\n1. 手机与电脑同一 WiFi\n2. 后端已启动（8080）\n3. config/env.ts 中 DEV_LAN_HOST 为本机 IP\n\n当前请求：${ENV.apiBase}`,
        showCancel: false,
      })
      return true
    }
    wx.showToast({ title: err.message, icon: 'none' })
    return true
  }
  return false
}
