import type { DoudizhuStateSync } from '../types/game'
import { sortHand } from './cards'

export interface PlayZoneView {
  faceCards: string[]
  backCount: number
  subtitle: string
}

export interface OptimisticPlay {
  seat: number
  cards: string[]
}

export function resolvePlayZoneView(
  state: DoudizhuStateSync,
  mySeat: number | undefined,
  optimistic: OptimisticPlay | null,
  cachedOwnCards: string[],
): { view: PlayZoneView; nextCachedOwnCards: string[] } {
  const lastPlay = state.lastPlay
  if (!lastPlay || !lastPlay.cardCount) {
    return {
      view: { faceCards: [], backCount: 0, subtitle: '' },
      nextCachedOwnCards: [],
    }
  }

  const { seat, cardCount } = lastPlay
  let nextCached = cachedOwnCards

  if (mySeat != null && seat === mySeat) {
    if (lastPlay.cards?.length === cardCount) {
      nextCached = sortHand(lastPlay.cards)
    } else if (optimistic && optimistic.cards.length === cardCount) {
      nextCached = [...optimistic.cards]
    } else if (cachedOwnCards.length === cardCount) {
      nextCached = cachedOwnCards
    } else {
      nextCached = []
    }

    if (nextCached.length === cardCount) {
      return {
        view: {
          faceCards: sortHand(nextCached),
          backCount: 0,
          subtitle: '你的出牌',
        },
        nextCachedOwnCards: nextCached,
      }
    }
  } else {
    nextCached = []
  }

  // 对手出牌展示在头像旁，中央出牌区仅保留阶段提示
  return {
    view: { faceCards: [], backCount: 0, subtitle: '' },
    nextCachedOwnCards: nextCached,
  }
}
