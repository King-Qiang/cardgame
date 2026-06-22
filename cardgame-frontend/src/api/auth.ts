import client from './client'
import type { ApiResponse, LoginResponse } from '../types/api'

export interface LoginRequest {
  username: string
  password: string
}

export async function login(data: LoginRequest) {
  const res = await client.post<ApiResponse<LoginResponse>>('/admin/auth/login', data)
  return res.data.data
}

export async function logout() {
  await client.post<ApiResponse<void>>('/admin/auth/logout')
}
