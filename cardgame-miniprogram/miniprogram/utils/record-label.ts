import { formatDateTime } from './transaction-label'

const GAME_TYPE_LABELS: Record<string, string> = {
  DOUDIZHU: '斗地主',
  MAHJONG: '麻将',
}

const MODE_LABELS: Record<string, string> = {
  FRIEND: '亲友',
  MATCH: '匹配',
  RANKED: '排位',
  PVE: '练习',
}

export function gameTypeLabel(gameType: string): string {
  return GAME_TYPE_LABELS[gameType] || gameType
}

export function modeLabel(mode: string): string {
  return MODE_LABELS[mode] || mode || '—'
}

export function formatDuration(sec: number): string {
  if (!sec || sec <= 0) return '—'
  const m = Math.floor(sec / 60)
  const s = sec % 60
  if (m <= 0) return `${s}秒`
  return `${m}分${s > 0 ? `${s}秒` : ''}`
}

export function formatGoldDelta(delta: number): string {
  const prefix = delta > 0 ? '+' : ''
  return `${prefix}${delta}金`
}

export function formatRecordTime(iso: string | undefined): string {
  return formatDateTime(iso)
}

export function shortRecordId(recordId: string): string {
  if (!recordId) return '—'
  return recordId.length > 10 ? `${recordId.slice(0, 10)}…` : recordId
}

export { formatDateTime }
