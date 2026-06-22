import client from './client'
import type { ApiResponse, PageResult } from '../types/api'

export interface RechargeOrderItem {
  orderNo: string
  userId: number
  amount: number
  goldAmount: number
  payChannel: string
  status: string
  paidAt?: string
  createdAt: string
}

export async function fetchOrders(params: Record<string, unknown>) {
  const res = await client.get<ApiResponse<PageResult<RechargeOrderItem>>>('/admin/orders', { params })
  return res.data.data
}
