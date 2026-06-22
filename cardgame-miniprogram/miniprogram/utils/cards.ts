export interface CardView {
  code: string
  rank: string
  suit: string
  suitSymbol: string
  isRed: boolean
  isJoker: boolean
}

const SUIT_MAP: Record<string, { symbol: string; red: boolean }> = {
  S: { symbol: '♠', red: false },
  H: { symbol: '♥', red: true },
  D: { symbol: '♦', red: true },
  C: { symbol: '♣', red: false },
}

export function parseCard(code: string): CardView {
  if (code === 'XJ') {
    return { code, rank: '小', suit: 'JOKER', suitSymbol: '🃏', isRed: false, isJoker: true }
  }
  if (code === 'DJ') {
    return { code, rank: '大', suit: 'JOKER', suitSymbol: '🃏', isRed: true, isJoker: true }
  }
  const rank = code.slice(0, -1)
  const suitKey = code.slice(-1)
  const suit = SUIT_MAP[suitKey] || { symbol: '?', red: false }
  return {
    code,
    rank,
    suit: suitKey,
    suitSymbol: suit.symbol,
    isRed: suit.red,
    isJoker: false,
  }
}

export function sortHand(cards: string[]): string[] {
  return [...cards].sort((a, b) => rankValue(b) - rankValue(a))
}

function rankValue(code: string): number {
  if (code === 'DJ') return 17
  if (code === 'XJ') return 16
  const rank = code.slice(0, -1)
  const order: Record<string, number> = {
    '3': 3, '4': 4, '5': 5, '6': 6, '7': 7, '8': 8, '9': 9,
    '10': 10, J: 11, Q: 12, K: 13, A: 14, '2': 15,
  }
  return order[rank] || 0
}
