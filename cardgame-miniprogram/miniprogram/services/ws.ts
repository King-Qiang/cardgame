import { ENV } from '../config/env'
import type { WsInboundType, WsMessage } from '../types/game'
import * as storage from '../utils/storage'

type MessageHandler = (msg: WsMessage) => void

const HEARTBEAT_MS = 30000

let socketTask: WechatMiniprogram.SocketTask | null = null
let connectPromise: Promise<void> | null = null
let connected = false
let boundRoomId = ''
let lastActionSeq = 0
let heartbeatTimer = 0

const handlers = new Map<string, Set<MessageHandler>>()

function buildWsUrl(): string {
  const token = encodeURIComponent(storage.getAccessToken())
  return `${ENV.wsBase}?token=${token}`
}

function sendRaw(data: Record<string, unknown>): void {
  if (!socketTask || !connected) {
    return
  }
  socketTask.send({
    data: JSON.stringify(data),
    fail: () => {
      connected = false
    },
  })
}

function dispatch(msg: WsMessage): void {
  handlers.get(msg.type)?.forEach((fn) => fn(msg))
  handlers.get('*')?.forEach((fn) => fn(msg))
}

function startHeartbeat(): void {
  stopHeartbeat()
  heartbeatTimer = setInterval(() => {
    sendRaw({ type: 'PING' })
  }, HEARTBEAT_MS) as unknown as number
}

function stopHeartbeat(): void {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = 0
  }
}

export function on(type: WsInboundType | '*', handler: MessageHandler): void {
  if (!handlers.has(type)) {
    handlers.set(type, new Set())
  }
  handlers.get(type)!.add(handler)
}

export function off(type: WsInboundType | '*', handler: MessageHandler): void {
  handlers.get(type)?.delete(handler)
}

export function isConnected(): boolean {
  return connected
}

export function getBoundRoomId(): string {
  return boundRoomId
}

export function syncActionSeq(seq: number): void {
  if (seq > lastActionSeq) {
    lastActionSeq = seq
  }
}

export function ensureConnected(): Promise<void> {
  if (connected && socketTask) {
    return Promise.resolve()
  }
  if (connectPromise) {
    return connectPromise
  }

  connectPromise = new Promise((resolve, reject) => {
    const token = storage.getAccessToken()
    if (!token) {
      connectPromise = null
      reject(new Error('未登录'))
      return
    }

    if (socketTask) {
      try {
        socketTask.close({})
      } catch {
        // ignore
      }
      socketTask = null
      connected = false
    }

    const task = wx.connectSocket({ url: buildWsUrl() })
    socketTask = task

    task.onOpen(() => {
      connected = true
      connectPromise = null
      startHeartbeat()
      resolve()
    })

    task.onMessage((res) => {
      try {
        const msg = JSON.parse(String(res.data)) as WsMessage
        dispatch(msg)
      } catch {
        // ignore malformed frame
      }
    })

    task.onClose(() => {
      connected = false
      stopHeartbeat()
      socketTask = null
    })

    task.onError(() => {
      connected = false
      connectPromise = null
      reject(new Error('WebSocket 连接失败'))
    })
  })

  return connectPromise
}

export function bindRoom(roomId: string): void {
  boundRoomId = roomId
  sendRaw({ type: 'BIND_ROOM', roomId })
}

export function reconnect(roomId: string): void {
  boundRoomId = roomId
  sendRaw({ type: 'RECONNECT', roomId })
}

export function sendAction(action: string, cards?: string[]): void {
  if (!boundRoomId) {
    return
  }
  const seq = lastActionSeq + 1
  const payload: Record<string, unknown> = { action }
  if (cards && cards.length > 0) {
    payload.cards = cards
  }
  sendRaw({
    type: 'ACTION',
    roomId: boundRoomId,
    seq,
    payload,
  })
}

export function close(): void {
  stopHeartbeat()
  boundRoomId = ''
  lastActionSeq = 0
  if (socketTask) {
    try {
      socketTask.close({})
    } catch {
      // ignore
    }
  }
  socketTask = null
  connected = false
  connectPromise = null
}
