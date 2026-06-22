import client from './client'
import type { ApiResponse, PageResult } from '../types/api'

export interface ShopItem {
  id: number
  name: string
  price: number
  currency: string
  payload: unknown
  status: number
  statusLabel: string
  sortOrder: number
  createdAt: string
  updatedAt: string
}

export interface ShopItemPayload {
  name: string
  price: number
  currency: 'GOLD' | 'DIAMOND'
  payload: unknown
  status: number
  sortOrder: number
}

export async function fetchShopItems(params: Record<string, unknown>) {
  const res = await client.get<ApiResponse<PageResult<ShopItem>>>('/admin/shop/items', { params })
  return res.data.data
}

export async function createShopItem(payload: ShopItemPayload) {
  const res = await client.post<ApiResponse<ShopItem>>('/admin/shop/items', payload)
  return res.data.data
}

export async function updateShopItem(id: number, payload: ShopItemPayload) {
  const res = await client.put<ApiResponse<ShopItem>>(`/admin/shop/items/${id}`, payload)
  return res.data.data
}

export async function deleteShopItem(id: number) {
  await client.delete(`/admin/shop/items/${id}`)
}
