export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  traceId?: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

export interface UserInfo {
  id: number
  nickname: string
  avatar: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
  user: UserInfo
}

export interface TokenPairResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

export interface WalletBalance {
  userId: number
  gold: number
}

export interface PlayerRank {
  userId: number
  gameType: string
  seasonId: string
  tier: string
  points: number
  wins: number
  losses: number
  nextTier?: string
  pointsToNextTier?: number
}

export interface SettlementItem {
  userId: number
  seat: number
  scoreDelta: number
  goldDelta: number
  multiplier: number
}

export interface PendingSettlement {
  roomId: string
  gameType: string
  mode: string
  settlements: SettlementItem[]
  result: {
    winnerSeat: number
    landlordSeat: number
    multiplier: number
  }
}

export interface RoomPlayer {
  userId: number
  nickname: string
  avatar: string
  seat: number
  ready: boolean
  isRobot?: boolean
}

export interface RoomDetail {
  roomId: string
  gameType: string
  mode: string
  status: string
  ownerId: number
  maxPlayers: number
  players: RoomPlayer[]
}

export type MatchMode = 'MATCH' | 'RANKED'

export type MatchStatus = 'NOT_IN_QUEUE' | 'WAITING' | 'MATCHED' | 'CANCELLED'

export interface QuickMatchResponse {
  status: MatchStatus
  roomId?: string
  queueSize: number
  requiredPlayers: number
  matchMode?: MatchMode
  matchTier?: string
  estimatedWaitSec?: number
}

export interface QuickMatchRequest {
  gameType: string
  mode: MatchMode
}

export interface CreateRoomRequest {
  gameType: string
  mode: string
  config: {
    baseScore: number
    enableGrab: boolean
    enableDouble: boolean
  }
}

export interface CreatePveRoomRequest {
  gameType: string
  config?: {
    baseScore?: number
    botDifficulty?: string
    botActionDelayMs?: number
  }
}

export interface RoomSeatView {
  seat: number
  occupied: boolean
  userId?: number
  nickname: string
  avatarText: string
  ready: boolean
  isOwner: boolean
  isSelf: boolean
  isRobot?: boolean
}

export interface ShopItem {
  id: number
  name: string
  price: number
  currency: string
  payload?: { gold?: number }
  status: number
  statusLabel: string
  sortOrder: number
}

export interface ShopBuyResult {
  itemId: number
  quantity: number
  totalCost: number
  balanceAfter: number
  grantedGold: number
}

export interface DailySignStatus {
  signedToday: boolean
  streakDay: number
  rewardGold: number
  nextRewardGold: number
  rewardPreview: number[]
}

export interface WalletTransaction {
  id: number
  userId: number
  type: string
  amount: number
  balanceAfter: number
  refType?: string
  refId?: string
  remark?: string
  createdAt: string
}

export interface RankLeaderboardItem {
  rank: number
  userId: number
  nickname: string
  tier: string
  points: number
  wins: number
  losses: number
}

export interface RechargeOrder {
  orderNo: string
  userId: number
  amount: number
  goldAmount: number
  payChannel: string
  status: string
  paidAt?: string
  createdAt: string
}

export interface CreateRechargeOrderRequest {
  amount: number
  goldAmount: number
  payChannel?: string
}

export interface RechargeTier {
  label: string
  amount: number
  goldAmount: number
}

export interface RankSummary {
  gameType: string
  tier: string
  points: number
  wins: number
  losses: number
}

export interface PlayerMe {
  id: number
  nickname: string
  avatar: string
  gold: number
  createdAt?: string
  rankSummary?: RankSummary | null
}

export interface UpdatePlayerProfileRequest {
  nickname?: string
  avatar?: string
}

export interface PlayerRecordListItem {
  recordId: string
  roomId: string
  gameType: string
  mode: string
  status: string
  startAt: string
  endAt: string
  durationSec: number
  myGoldDelta: number
  myScore: number
  isWin: boolean
  multiplier?: number
}

export interface PlayerRecordParticipant {
  userId: number
  nickname: string
  seat: number | null
  goldDelta: number
  isLandlord: boolean
}

export interface PlayerRecordDetail {
  recordId: string
  roomId: string
  gameType: string
  mode: string
  status: string
  startAt: string
  endAt: string
  durationSec: number
  result: {
    winnerSeat?: number
    landlordSeat?: number
    multiplier?: number
  }
  mySettlement: {
    seat: number | null
    goldDelta: number
    score: number
    isLandlord: boolean
    isWin: boolean
  }
  participants: PlayerRecordParticipant[]
}

export interface GameActionLog {
  id: number
  recordId: string
  seq: number
  userId: number
  action: string
  payload?: Record<string, unknown>
  createdAt: string
}
