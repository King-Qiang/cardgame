import type { RoomPlayer } from '../types/api'
import type { DoudizhuStateSync, OpponentView } from '../types/game'
import type { SeatTableMap } from './seat-table'
import { seatTableToOpponentFields } from './seat-table'

export function adjacentSeats(mySeat: number): { left: number; right: number } {
  return {
    left: (mySeat + 2) % 3,
    right: (mySeat + 1) % 3,
  }
}

export function buildPlayerMap(players: RoomPlayer[]): Map<number, RoomPlayer> {
  const map = new Map<number, RoomPlayer>()
  players.forEach((p) => map.set(p.seat, p))
  return map
}

export function buildOpponents(
  state: DoudizhuStateSync,
  playerMap: Map<number, RoomPlayer>,
  currentSeat: number,
  seatTable: SeatTableMap,
): OpponentView[] {
  const mySeat = state.mySeat ?? 0
  const { left, right } = adjacentSeats(mySeat)
  return [left, right].map((seat) => {
    const player = playerMap.get(seat)
    const nickname = player?.nickname || `玩家${seat + 1}`
    const table = seatTableToOpponentFields(seatTable[seat])
    return {
      seat,
      nickname,
      avatarText: nickname.slice(0, 1),
      handCount: state.handCounts?.[String(seat)] ?? 0,
      isLandlord: state.landlordSeat === seat,
      isActive: currentSeat === seat,
      showPass: table.showPass,
      passLabel: table.passLabel,
      tableCards: table.tableCards,
      tableBackCount: table.tableBackCount,
    }
  })
}

export function isMyTurn(state: DoudizhuStateSync): boolean {
  return state.mySeat != null && state.currentSeat === state.mySeat
}

export function canPassInPlaying(state: DoudizhuStateSync): boolean {
  return isMyTurn(state) && state.phase === 'PLAYING' && !!state.lastPlay
}

export function canPlayCards(state: DoudizhuStateSync, selectedCount: number): boolean {
  return isMyTurn(state) && state.phase === 'PLAYING' && selectedCount > 0
}

export function canBid(state: DoudizhuStateSync): boolean {
  return isMyTurn(state) && state.phase === 'BIDDING'
}
