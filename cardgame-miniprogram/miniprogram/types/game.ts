export type GamePhase = 'BIDDING' | 'PLAYING' | 'FINISHED'

export type WsOutboundType = 'PING' | 'ACTION' | 'RECONNECT' | 'BIND_ROOM'

export type WsInboundType =
  | 'STATE_SYNC'
  | 'ACTION_RESULT'
  | 'SETTLEMENT'
  | 'ROOM_EVENT'
  | 'ERROR'
  | 'PONG'
  | 'AUTO_PLAY'

export interface WsMessage<T = unknown> {
  type: WsInboundType | WsOutboundType
  roomId?: string
  seq?: number
  payload: T
}

export interface LastPlayState {
  seat: number
  cardCount: number
  /** 本轮桌面出牌（全员可见） */
  cards?: string[]
}

export interface DoudizhuStateSync {
  recordId: string
  roomId: string
  gameType: string
  phase: GamePhase
  currentSeat: number
  landlordSeat: number
  multiplier: number
  actionSeq: number
  mySeat?: number
  myHand?: string[]
  lastPlay?: LastPlayState | null
  handCounts?: Record<string, number>
}

export interface ActionResultPayload {
  success: boolean
  message: string
  userId: number
}

export interface SettlementPayload {
  settlements: {
    userId: number
    seat: number
    scoreDelta: number
    goldDelta: number
    multiplier: number
  }[]
  result: {
    winnerSeat: number
    landlordSeat: number
    multiplier: number
  }
}

export interface RoomEventPayload {
  event: string
  userId?: number
  ready?: boolean
  [key: string]: unknown
}

export interface OpponentView {
  seat: number
  nickname: string
  avatarText: string
  handCount: number
  isLandlord: boolean
  isActive: boolean
  /** 当前轮次该对手出的牌（牌面） */
  tableCards: string[]
  tableBackCount: number
  /** 当前轮次 pass 展示（不出/不叫） */
  showPass: boolean
  passLabel: string
}
