export function formatGold(value: number | undefined | null): string {
  const n = value ?? 0
  return n.toLocaleString('zh-CN')
}

export function formatTierLabel(tier: string, points: number): string {
  return `${tierLabel(tier)} ${points}分`
}

function tierLabel(tier: string): string {
  const map: Record<string, string> = {
    BRONZE: '青铜',
    SILVER: '白银',
    GOLD: '黄金',
    PLATINUM: '铂金',
    DIAMOND: '钻石',
  }
  return map[tier] || tier
}

export { tierLabel }

export function tierColor(tier: string): string {
  const map: Record<string, string> = {
    BRONZE: '#cd7f32',
    SILVER: '#b0bec5',
    GOLD: '#f5c842',
    PLATINUM: '#4fc3f7',
    DIAMOND: '#ba68c8',
  }
  return map[tier] || '#ffffff'
}
