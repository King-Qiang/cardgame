import type { CreateRechargeOrderRequest, RechargeOrder } from '../types/api'
import * as request from './request'

export function createOrder(body: CreateRechargeOrderRequest): Promise<RechargeOrder> {
  return request.post<RechargeOrder>('/orders', body)
}

export function payCallback(orderNo: string): Promise<RechargeOrder> {
  return request.post<RechargeOrder>(`/orders/${encodeURIComponent(orderNo)}/pay-callback`)
}
