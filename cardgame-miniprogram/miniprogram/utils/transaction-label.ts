const TYPE_LABELS: Record<string, string> = {
  GAME_WIN: '对局奖励',
  GAME_LOSE: '对局扣除',
  RECHARGE: '充值',
  ADMIN_ADJUST: '系统调整',
  SHOP_BUY: '商城',
  DAILY_REWARD: '每日签到',
  ROOM_FEE: '房费',
}

export function transactionTypeLabel(type: string): string {
  return TYPE_LABELS[type] || type
}

export function formatDateTime(iso: string | undefined): string {
  if (!iso) return '—'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso.slice(0, 16).replace('T', ' ')
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mi = String(d.getMinutes()).padStart(2, '0')
  return `${mm}-${dd} ${hh}:${mi}`
}

export function buildSignCells(status: {
  signedToday: boolean
  streakDay: number
  rewardGold: number
  rewardPreview: number[]
}): { day: number; reward: number; signed: boolean; today: boolean }[] {
  const rewards = status.rewardPreview?.length
    ? status.rewardPreview
    : [100, 100, 200, 200, 300, 300, 500]
  const todayIndex = status.signedToday
    ? Math.max(0, status.streakDay - 1)
    : Math.max(0, rewards.findIndex((r) => r === status.rewardGold))
  return rewards.map((reward, i) => ({
    day: i + 1,
    reward,
    signed: status.signedToday ? i < status.streakDay : false,
    today: i === todayIndex,
  }))
}

export function shopItemGrantedGold(item: { payload?: { gold?: number } }): number {
  return item.payload?.gold ?? 0
}

export const RECHARGE_TIERS = [
  { label: '¥10', amount: 10, goldAmount: 1000 },
  { label: '¥45', amount: 45, goldAmount: 5000 },
  { label: '¥80', amount: 80, goldAmount: 10000 },
]
