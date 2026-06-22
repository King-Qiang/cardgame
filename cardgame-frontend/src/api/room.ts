import client from './client'
import type { ApiResponse, PageResult } from '../types/api'

export interface RoomListItem {
  roomId: string
  gameType: string
  mode: string
  status: string
  ownerId: number
  playerCount: number
  createdAt: string
}

export interface RoomPlayer {
  userId: number
  nickname: string
  avatar: string
  seat: number
  ready: boolean
  isRobot?: boolean
}

export interface RoomDetail {
  roomId: string
  gameType: string
  mode: string
  status: string
  ownerId: number
  maxPlayers: number
  players: RoomPlayer[]
  createdAt: string
}

export async function fetchRooms(params: Record<string, unknown>) {
  const res = await client.get<ApiResponse<PageResult<RoomListItem>>>('/admin/rooms', { params })
  return res.data.data
}

export async function fetchRoomDetail(roomId: string) {
  const res = await client.get<ApiResponse<RoomDetail>>(`/admin/rooms/${roomId}`)
  return res.data.data
}

export async function disbandRoom(roomId: string, reason: string) {
  await client.post(`/admin/rooms/${roomId}/disband`, { reason })
}

export async function kickRoomPlayer(roomId: string, userId: number, reason: string) {
  await client.post(`/admin/rooms/${roomId}/kick`, { userId, reason })
}
