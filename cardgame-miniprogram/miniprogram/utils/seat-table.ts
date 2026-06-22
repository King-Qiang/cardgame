import type { DoudizhuStateSync } from '../types/game'
import { sortHand } from './cards'

export type SeatTableDisplay =
  | { kind: 'play'; cards: string[]; backCount?: number }
  | { kind: 'pass'; label: '不出' | '不叫' }

export type SeatTableMap = Record<number, SeatTableDisplay | null>

export function createEmptySeatTable(): SeatTableMap {
  return { 0: null, 1: null, 2: null }
}

export function seatTableToOpponentFields(display: SeatTableDisplay | null | undefined): {
  tableCards: string[]
  tableBackCount: number
  showPass: boolean
  passLabel: string
} {
  if (!display) {
    return { tableCards: [], tableBackCount: 0, showPass: false, passLabel: '' }
  }
  if (display.kind === 'play') {
    return {
      tableCards: display.cards,
      tableBackCount: display.cards.length > 0 ? 0 : display.backCount ?? 0,
      showPass: false,
      passLabel: '',
    }
  }
  return {
    tableCards: [],
    tableBackCount: 0,
    showPass: true,
    passLabel: display.label,
  }
}

/** 根据 ACTION_RESULT 的 userId + 紧随其后的 STATE_SYNC 更新该座位展示（直到该座位再次行动） */
export function applyActedUserToSeatTable(
  table: SeatTableMap,
  state: DoudizhuStateSync,
  actedSeat: number,
  phaseBefore: string,
): SeatTableMap {
  const next: SeatTableMap = { ...table }
  const last = state.lastPlay

  if (last && last.seat === actedSeat && (last.cardCount ?? 0) > 0) {
    next[actedSeat] = readPlayDisplay(last)
    return next
  }

  if (phaseBefore === 'BIDDING' && state.phase === 'PLAYING' && actedSeat === state.landlordSeat) {
    next[actedSeat] = null
    return next
  }

  if (state.phase === 'BIDDING') {
    next[actedSeat] = { kind: 'pass', label: '不叫' }
    return next
  }

  if (state.phase === 'PLAYING') {
    next[actedSeat] = { kind: 'pass', label: '不出' }
  }

  return next
}

function readPlayDisplay(last: NonNullable<DoudizhuStateSync['lastPlay']>): SeatTableDisplay {
  if (last.cards?.length) {
    return { kind: 'play', cards: sortHand(last.cards) }
  }
  return { kind: 'play', cards: [], backCount: last.cardCount ?? 0 }
}

export function clearSeatTableForPhase(table: SeatTableMap, phase: string): SeatTableMap {
  if (phase === 'FINISHED') {
    return createEmptySeatTable()
  }
  return table
}
