import type { PageResult, WalletBalance, WalletTransaction } from '../types/api'
import * as request from './request'

export function balance(): Promise<WalletBalance> {
  return request.get<WalletBalance>('/wallet')
}

export function transactions(page = 1, pageSize = 20): Promise<PageResult<WalletTransaction>> {
  return request.get<PageResult<WalletTransaction>>('/wallet/transactions', { page, pageSize })
}
