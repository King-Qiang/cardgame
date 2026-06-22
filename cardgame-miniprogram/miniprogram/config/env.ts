/** 真机调试：改成你电脑的局域网 IP（Mac: ipconfig getifaddr en0） */
const DEV_LAN_HOST = '172.16.22.109'

const DEV_PORT = 8080

function resolveDevHost(): string {
  try {
    const { platform } = wx.getDeviceInfo()
    if (platform === 'devtools') {
      return '127.0.0.1'
    }
  } catch {
    // 非小程序运行时（如 tsc）回退局域网地址
  }
  return DEV_LAN_HOST
}

function buildDevUrl(scheme: 'http' | 'ws', path: string): string {
  return `${scheme}://${resolveDevHost()}:${DEV_PORT}${path}`
}

/** 本地开发：微信开发者工具勾选「不校验合法域名」；真机需与 DEV_LAN_HOST 同一 WiFi */
export const ENV = {
  get apiBase() {
    return buildDevUrl('http', '/api/v1')
  },
  get wsBase() {
    return buildDevUrl('ws', '/ws/game')
  },
}
