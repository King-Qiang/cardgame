import type { CreatePveRoomRequest, CreateRoomRequest, RoomDetail } from '../types/api'
import * as request from './request'

export function create(body: CreateRoomRequest): Promise<RoomDetail> {
  return request.post<RoomDetail>('/rooms', body)
}

export function createPve(body: CreatePveRoomRequest): Promise<RoomDetail> {
  return request.post<RoomDetail>('/rooms/pve', body)
}

export function addBot(roomId: string, count = 1): Promise<RoomDetail> {
  return request.post<RoomDetail>(`/rooms/${roomId}/bots`, { count })
}

export function removeBot(roomId: string, seat: number): Promise<RoomDetail> {
  return request.del<RoomDetail>(`/rooms/${roomId}/bots/${seat}`)
}

export function join(roomId: string): Promise<RoomDetail> {
  return request.post<RoomDetail>(`/rooms/${roomId}/join`)
}

export function detail(roomId: string): Promise<RoomDetail> {
  return request.get<RoomDetail>(`/rooms/${roomId}`)
}

export function leave(roomId: string): Promise<RoomDetail> {
  return request.post<RoomDetail>(`/rooms/${roomId}/leave`)
}

export function ready(roomId: string, ready: boolean): Promise<RoomDetail> {
  return request.post<RoomDetail>(`/rooms/${roomId}/ready`, { ready })
}

export function start(roomId: string): Promise<RoomDetail> {
  return request.post<RoomDetail>(`/rooms/${roomId}/start`)
}
