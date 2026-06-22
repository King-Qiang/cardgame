const MODE_LABELS: Record<string, string> = {
  FRIEND: '亲友',
  MATCH: '匹配',
  RANKED: '排位',
  PVE: '练习',
}

export function roomModeLabel(mode: string | undefined | null): string {
  if (!mode) return '—'
  return MODE_LABELS[mode] || mode
}

export function isBotUserId(userId: number | undefined | null): boolean {
  return userId != null && userId >= 900_001 && userId <= 900_003
}
