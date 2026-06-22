import type { ActivityType, DailySignConfig, NewbieGiftConfig } from '../types/activity'

export const DEFAULT_DAILY_REWARDS = [100, 100, 200, 200, 300, 300, 500]

export function defaultConfigForType(type: ActivityType): unknown {
  switch (type) {
    case 'DAILY_SIGN':
      return defaultDailySignConfig()
    case 'NEWBIE_GIFT':
      return defaultNewbieGiftConfig()
    case 'LIMITED':
      return { rules: {} }
  }
}

export function defaultDailySignConfig(): DailySignConfig {
  return {
    rewards: [...DEFAULT_DAILY_REWARDS],
    description: '连续签到 7 天循环奖励',
  }
}

export function defaultNewbieGiftConfig(): NewbieGiftConfig {
  return { gold: 1000, items: [] }
}

export function parseDailySignConfig(raw: unknown): DailySignConfig {
  const defaults = defaultDailySignConfig()
  if (!raw || typeof raw !== 'object') {
    return defaults
  }
  const obj = raw as Record<string, unknown>
  const rewards: number[] = []
  if (Array.isArray(obj.rewards)) {
    for (const item of obj.rewards) {
      const n = Number(item)
      rewards.push(Number.isFinite(n) ? n : 0)
    }
  }
  while (rewards.length < 7) {
    rewards.push(defaults.rewards[rewards.length] ?? 0)
  }
  return {
    rewards: rewards.slice(0, 7),
    description: typeof obj.description === 'string' ? obj.description : defaults.description,
  }
}

export function parseNewbieGiftConfig(raw: unknown): NewbieGiftConfig {
  const defaults = defaultNewbieGiftConfig()
  if (!raw || typeof raw !== 'object') {
    return defaults
  }
  const obj = raw as Record<string, unknown>
  const gold = Number(obj.gold)
  const items = Array.isArray(obj.items) ? obj.items : []
  return {
    gold: Number.isFinite(gold) ? gold : defaults.gold,
    items,
  }
}

export function validateDailySignConfig(config: DailySignConfig): string | null {
  if (config.rewards.length !== 7) {
    return '每日签到奖励须配置 7 天'
  }
  if (config.rewards.some((v) => !Number.isFinite(v) || v < 0)) {
    return '每日奖励须为 ≥ 0 的数字'
  }
  return null
}

export function validateNewbieGiftConfig(config: NewbieGiftConfig): string | null {
  if (!Number.isFinite(config.gold) || config.gold < 0) {
    return '新手礼包金币须为 ≥ 0 的数字'
  }
  if (!Array.isArray(config.items)) {
    return '道具配置须为 JSON 数组'
  }
  return null
}

export function validateLimitedConfigJson(text: string): { ok: true; value: Record<string, unknown> } | { ok: false; message: string } {
  try {
    const parsed = JSON.parse(text) as unknown
    if (parsed === null || typeof parsed !== 'object' || Array.isArray(parsed)) {
      return { ok: false, message: '限时活动规则须为 JSON 对象' }
    }
    return { ok: true, value: parsed as Record<string, unknown> }
  } catch {
    return { ok: false, message: '活动规则 JSON 格式不正确' }
  }
}

export function sumDailyRewards(rewards: number[]): number {
  return rewards.reduce((sum, v) => sum + (Number.isFinite(v) ? v : 0), 0)
}
