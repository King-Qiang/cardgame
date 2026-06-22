export function formatGold(amount: number): string {
  return `${amount.toLocaleString('zh-CN')} 金币`
}

export function formatDateTime(iso?: string | null): string {
  if (!iso) return '—'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  return d.toLocaleString('zh-CN', { hour12: false })
}

export function maskOpenid(openid: string): string {
  if (!openid || openid.length <= 8) return openid
  return `${openid.slice(0, 4)}***${openid.slice(-4)}`
}
