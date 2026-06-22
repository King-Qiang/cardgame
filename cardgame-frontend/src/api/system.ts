import client from './client'
import type { ApiResponse, PageResult } from '../types/api'

export interface SystemConfigItem {
  configKey: string
  configValue: unknown
  description: string
  updatedAt: string
}

export interface AdminRoleItem {
  id: number
  name: string
  description: string
  permissions: string[]
  permissionCount: number
  createdAt: string
  updatedAt: string
}

export interface AdminAccountItem {
  id: number
  username: string
  realName: string
  roleId: number
  roleName: string
  status: number
  statusLabel: string
  lastLoginAt?: string
  lastLoginIp?: string
  createdAt: string
}

export interface OperationLogItem {
  id: number
  operatorId: number
  operatorName: string
  action: string
  targetType: string
  targetId: string
  detail?: Record<string, unknown>
  ip: string
  createdAt: string
}

export async function fetchSystemConfigs() {
  const res = await client.get<ApiResponse<SystemConfigItem[]>>('/admin/system/configs')
  return res.data.data
}

export async function fetchSystemConfig(key: string) {
  const res = await client.get<ApiResponse<SystemConfigItem>>(`/admin/system/configs/${key}`)
  return res.data.data
}

export async function updateSystemConfig(key: string, configValue: unknown) {
  const res = await client.put<ApiResponse<SystemConfigItem>>(`/admin/system/configs/${key}`, { configValue })
  return res.data.data
}

export async function fetchRoles() {
  const res = await client.get<ApiResponse<AdminRoleItem[]>>('/admin/roles')
  return res.data.data
}

export async function createRole(payload: { name: string; description: string; permissions: string[] }) {
  const res = await client.post<ApiResponse<AdminRoleItem>>('/admin/roles', payload)
  return res.data.data
}

export async function updateRole(id: number, payload: { name: string; description: string; permissions: string[] }) {
  const res = await client.put<ApiResponse<AdminRoleItem>>(`/admin/roles/${id}`, payload)
  return res.data.data
}

export async function deleteRole(id: number) {
  await client.delete(`/admin/roles/${id}`)
}

export async function fetchAdmins(params: Record<string, unknown>) {
  const res = await client.get<ApiResponse<PageResult<AdminAccountItem>>>('/admin/admins', { params })
  return res.data.data
}

export async function createAdmin(payload: {
  username: string
  password: string
  realName: string
  roleId: number
}) {
  const res = await client.post<ApiResponse<AdminAccountItem>>('/admin/admins', payload)
  return res.data.data
}

export async function updateAdmin(
  id: number,
  payload: { roleId: number; status: number; realName?: string }
) {
  const res = await client.put<ApiResponse<AdminAccountItem>>(`/admin/admins/${id}`, payload)
  return res.data.data
}

export async function resetAdminPassword(id: number, newPassword: string) {
  await client.post(`/admin/admins/${id}/reset-password`, { newPassword })
}

export async function fetchOperationLogs(params: Record<string, unknown>) {
  const res = await client.get<ApiResponse<PageResult<OperationLogItem>>>('/admin/operation-logs', { params })
  return res.data.data
}
