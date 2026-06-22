import type { GameActionLogItem } from '../api/record'

export interface DoudizhuSeatState {
  userId: number
  handCount: number
  isLandlord: boolean
}

export interface DoudizhuBoardState {
  seats: DoudizhuSeatState[]
  phase: 'BID' | 'PLAY' | 'SETTLED' | 'WAITING'
  landlordSeat: number | null
  bottomCards: string[]
  lastPlay: { seat: number; cards: string[] } | null
  currentStep: number
  currentAction: string
  currentDescription: string
}

const INITIAL_BOARD: DoudizhuBoardState = {
  seats: [],
  phase: 'WAITING',
  landlordSeat: null,
  bottomCards: [],
  lastPlay: null,
  currentStep: 0,
  currentAction: '',
  currentDescription: '',
}

function formatCard(card: string): string {
  const suitMap: Record<string, string> = { S: '♠', H: '♥', D: '♦', C: '♣' }
  if (card === 'XJ') return '小王'
  if (card === 'DJ') return '大王'
  const suit = card.slice(-1)
  const rank = card.slice(0, -1)
  const red = suit === 'H' || suit === 'D'
  return `${rank}${suitMap[suit] ?? suit}${red ? '' : ''}`
}

function describeAction(action: GameActionLogItem): string {
  const payload = action.payload ?? {}
  const seat = payload.seat as number | undefined
  switch (action.action) {
    case 'GAME_START':
      return '游戏开始'
    case 'DEAL':
      return '发牌完成'
    case 'BID':
      return payload.passed ? `座位 ${seat} 不叫地主` : `座位 ${seat} 叫地主`
    case 'LANDLORD':
      return `座位 ${seat} 成为地主`
    case 'PLAY_CARDS': {
      const cards = (payload.cards as string[] | undefined) ?? []
      return `座位 ${seat} 出牌：${cards.map(formatCard).join(' ')}`
    }
    case 'PASS':
      return `座位 ${seat} 过牌`
    case 'SETTLEMENT':
      return '对局结算'
    default:
      return action.action
  }
}

export function buildBoardAtStep(actions: GameActionLogItem[], step: number): DoudizhuBoardState {
  const board: DoudizhuBoardState = {
    ...INITIAL_BOARD,
    seats: [],
    currentStep: step,
  }

  const slice = actions.slice(0, step + 1)
  for (const action of slice) {
    const payload = action.payload ?? {}
    const seat = payload.seat as number | undefined

    switch (action.action) {
      case 'GAME_START': {
        const players = payload.players as Record<string, number> | undefined
        const handCounts = (slice.find((a) => a.action === 'DEAL')?.payload?.handCounts ?? {}) as Record<
          string,
          number
        >
        board.seats = Object.entries(players ?? {}).map(([s, userId]) => ({
          userId,
          handCount: handCounts[s] ?? 17,
          isLandlord: false,
        }))
        board.phase = 'BID'
        break
      }
      case 'LANDLORD': {
        board.landlordSeat = seat ?? null
        board.bottomCards = (payload.bottomCards as string[] | undefined) ?? []
        board.phase = 'PLAY'
        board.seats = board.seats.map((s, idx) => ({
          ...s,
          isLandlord: idx === seat,
          handCount: idx === seat ? s.handCount + board.bottomCards.length : s.handCount,
        }))
        break
      }
      case 'PLAY_CARDS': {
        const cards = (payload.cards as string[] | undefined) ?? []
        board.lastPlay = seat != null ? { seat, cards } : null
        if (seat != null) {
          board.seats = board.seats.map((s, idx) =>
            idx === seat ? { ...s, handCount: Math.max(0, s.handCount - cards.length) } : s,
          )
        }
        break
      }
      case 'PASS':
        break
      case 'SETTLEMENT':
        board.phase = 'SETTLED'
        break
      default:
        break
    }
    board.currentAction = action.action
    board.currentDescription = describeAction(action)
  }

  if (slice.length === 0) {
    board.currentDescription = '暂无操作'
  }
  return board
}

export function formatCardLabel(card: string): string {
  return formatCard(card)
}
