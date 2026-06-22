export type ActivityType = 'DAILY_SIGN' | 'NEWBIE_GIFT' | 'LIMITED'

export interface DailySignConfig {
  rewards: number[]
  description?: string
}

export interface NewbieGiftConfig {
  gold: number
  items: unknown[]
}

export interface LimitedActivityConfig {
  [key: string]: unknown
}
