const DEFAULT_FALLBACK = '/pages/index/index'

/** 规范化小程序页面路径，非法时返回 null */
export function normalizePageUrl(url: unknown): string | null {
  if (typeof url !== 'string') {
    return null
  }
  const trimmed = url.trim()
  if (!trimmed || trimmed === '/' || trimmed === 'undefined' || trimmed === 'null') {
    return null
  }
  if (!trimmed.startsWith('/')) {
    return null
  }
  const path = trimmed.split('?')[0]
  if (!path || path === '/') {
    return null
  }
  return trimmed
}

function logInvalidNavigate(api: string, url: unknown): void {
  console.error(`[navigate] ${api} 收到非法 url:`, url)
}

export function safeRedirectTo(url: string, fallback = DEFAULT_FALLBACK): void {
  const normalized = normalizePageUrl(url)
  if (!normalized) {
    logInvalidNavigate('redirectTo', url)
    wx.redirectTo({ url: fallback })
    return
  }
  wx.redirectTo({
    url: normalized,
    fail: (err) => {
      console.error('[navigate] redirectTo fail:', normalized, err)
      wx.redirectTo({ url: fallback })
    },
  })
}

export function safeReLaunch(url: string, fallback = DEFAULT_FALLBACK): void {
  const normalized = normalizePageUrl(url)
  if (!normalized) {
    logInvalidNavigate('reLaunch', url)
    wx.reLaunch({ url: fallback })
    return
  }
  wx.reLaunch({
    url: normalized,
    fail: (err) => {
      console.error('[navigate] reLaunch fail:', normalized, err)
      wx.reLaunch({ url: fallback })
    },
  })
}

export function safeNavigateTo(url: string, fallback = DEFAULT_FALLBACK): void {
  const normalized = normalizePageUrl(url)
  if (!normalized) {
    logInvalidNavigate('navigateTo', url)
    wx.reLaunch({ url: fallback })
    return
  }
  wx.navigateTo({
    url: normalized,
    fail: (err) => {
      console.error('[navigate] navigateTo fail:', normalized, err)
      wx.reLaunch({ url: fallback })
    },
  })
}

/** App.onPageNotFound / wx.onPageNotFound 统一回退 */
export function handlePageNotFound(path: string, query: Record<string, string>): void {
  console.error('[app] onPageNotFound:', { path, query })
  safeReLaunch(DEFAULT_FALLBACK)
}
