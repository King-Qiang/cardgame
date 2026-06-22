export const RANK_TIERS = ['BRONZE', 'SILVER', 'GOLD', 'PLATINUM', 'DIAMOND'] as const

export type RankTier = (typeof RANK_TIERS)[number]

export const TIER_LABELS: Record<string, string> = {
  BRONZE: '青铜',
  SILVER: '白银',
  GOLD: '黄金',
  PLATINUM: '铂金',
  DIAMOND: '钻石',
}

export function tierLabel(tier: string): string {
  return TIER_LABELS[tier] ?? tier
}

export function tierTagType(tier: string): 'info' | 'success' | 'warning' | 'danger' | '' {
  switch (tier) {
    case 'BRONZE':
      return 'info'
    case 'SILVER':
      return ''
    case 'GOLD':
      return 'warning'
    case 'PLATINUM':
      return 'success'
    case 'DIAMOND':
      return 'danger'
    default:
      return 'info'
  }
}

export const TIER_OPTIONS = RANK_TIERS.map((value) => ({
  label: TIER_LABELS[value],
  value,
}))

export const GAME_TYPE_OPTIONS = [{ label: '斗地主', value: 'DOUDIZHU' }]
