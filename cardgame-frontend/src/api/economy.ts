import client from './client'
import type { ApiResponse, PageResult } from '../types/api'

export interface WalletTransactionItem {
  id: number
  userId: number
  type: string
  amount: number
  balanceAfter: number
  refType: string
  refId: string
  remark: string
  createdAt: string
}

export interface AdjustRequestItem {
  id: number
  userId: number
  adjustType: string
  amount: number
  reason: string
  status: string
  applicantId: number
  approverId?: number
  approvedAt?: string
  rejectReason?: string
  createdAt: string
}

export interface AdjustWalletPayload {
  adjustType: 'INCREASE' | 'DECREASE'
  amount: number
  reason: string
}

export async function fetchTransactions(params: Record<string, unknown>) {
  const res = await client.get<ApiResponse<PageResult<WalletTransactionItem>>>('/admin/wallet/transactions', {
    params,
  })
  return res.data.data
}

export async function adjustWallet(userId: number, payload: AdjustWalletPayload) {
  const res = await client.post<ApiResponse<unknown>>(`/admin/users/${userId}/adjust-wallet`, payload)
  return res.data.data
}

export async function createAdjustRequest(payload: AdjustWalletPayload & { userId: number }) {
  const res = await client.post<ApiResponse<AdjustRequestItem>>('/admin/wallet/adjust-requests', payload)
  return res.data.data
}

export async function fetchAdjustRequests(params: Record<string, unknown>) {
  const res = await client.get<ApiResponse<PageResult<AdjustRequestItem>>>('/admin/wallet/adjust-requests', {
    params,
  })
  return res.data.data
}

export async function approveAdjustRequest(id: number) {
  const res = await client.post<ApiResponse<AdjustRequestItem>>(`/admin/wallet/adjust-requests/${id}/approve`)
  return res.data.data
}

export async function rejectAdjustRequest(id: number, rejectReason: string) {
  const res = await client.post<ApiResponse<AdjustRequestItem>>(`/admin/wallet/adjust-requests/${id}/reject`, {
    rejectReason,
  })
  return res.data.data
}
