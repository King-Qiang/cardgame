import type { SettlementItem } from '../types/api'
import { formatGoldDelta } from './record-label'

export function findMySettlement(
  settlements: SettlementItem[] | undefined,
  myUserId: number | undefined,
): SettlementItem | undefined {
  if (!myUserId || !settlements?.length) {
    return undefined
  }
  const uid = Number(myUserId)
  return settlements.find((s) => Number(s.userId) === uid)
}

export function resolveIsWin(
  me: SettlementItem | undefined,
  winnerSeat: number | null | undefined,
): boolean {
  if (me) {
    if (me.goldDelta > 0) return true
    if (me.goldDelta < 0) return false
  }
  if (me?.seat == null || winnerSeat == null) {
    return false
  }
  return Number(winnerSeat) === Number(me.seat)
}

export function formatSettlementGold(delta: number): string {
  return formatGoldDelta(delta)
}
