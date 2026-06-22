export type GameType = 'DOUDIZHU' | 'MAHJONG'

export const DEFAULT_GAME_TYPE: GameType = 'DOUDIZHU'

export const TURN_TIMEOUT_SEC = 30

export interface GameMeta {
  gameType: GameType
  name: string
  minPlayers: number
  maxPlayers: number
  matchLabel: string
  rankedSupported: boolean
}

export const GAME_CATALOG: Record<GameType, GameMeta> = {
  DOUDIZHU: {
    gameType: 'DOUDIZHU',
    name: '斗地主',
    minPlayers: 3,
    maxPlayers: 3,
    matchLabel: '3 人局',
    rankedSupported: true,
  },
  MAHJONG: {
    gameType: 'MAHJONG',
    name: '麻将',
    minPlayers: 4,
    maxPlayers: 4,
    matchLabel: '4 人局',
    rankedSupported: false,
  },
}

export type DockTab = 'lobby' | 'records' | 'profile'

export const DOCK_TABS: { id: DockTab; label: string; icon: string; path: string }[] = [
  { id: 'lobby', label: '大厅', icon: '🏠', path: '/pages/lobby/lobby' },
  { id: 'records', label: '战绩', icon: '📋', path: '/pages/records/records' },
  { id: 'profile', label: '我的', icon: '👤', path: '/pages/profile/profile' },
]
