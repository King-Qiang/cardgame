import type { RoomDetail, RoomPlayer, RoomSeatView, UserInfo } from '../types/api'

export function buildSeatViews(
  detail: RoomDetail,
  myUserId: number | undefined,
): RoomSeatView[] {
  const seats: RoomSeatView[] = []
  for (let seat = 0; seat < detail.maxPlayers; seat += 1) {
    const player = detail.players.find((p) => p.seat === seat)
    seats.push(toSeatView(seat, player, detail.ownerId, myUserId))
  }
  return seats
}

function toSeatView(
  seat: number,
  player: RoomPlayer | undefined,
  ownerId: number,
  myUserId: number | undefined,
): RoomSeatView {
  if (!player) {
    return {
      seat,
      occupied: false,
      nickname: '等待加入…',
      avatarText: '+',
      ready: false,
      isOwner: false,
      isSelf: false,
    }
  }
  const nickname = player.nickname || `玩家${player.userId}`
  return {
    seat,
    occupied: true,
    userId: player.userId,
    nickname,
    avatarText: nickname.slice(0, 1),
    ready: player.ready,
    isOwner: player.userId === ownerId,
    isSelf: player.userId === myUserId,
    isRobot: !!player.isRobot,
  }
}

export function getMyPlayer(detail: RoomDetail, myUserId: number | undefined): RoomPlayer | undefined {
  if (!myUserId) return undefined
  return detail.players.find((p) => p.userId === myUserId)
}

export function getUserInfoId(): number | undefined {
  const user = wx.getStorageSync('user_info') as UserInfo | null
  return user?.id
}
