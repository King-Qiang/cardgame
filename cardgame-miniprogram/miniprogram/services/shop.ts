import type { ShopBuyResult, ShopItem } from '../types/api'
import * as request from './request'

export function listItems(): Promise<ShopItem[]> {
  return request.get<ShopItem[]>('/shop/items')
}

export function buy(itemId: number, quantity = 1): Promise<ShopBuyResult> {
  return request.post<ShopBuyResult>('/shop/buy', { itemId, quantity })
}
