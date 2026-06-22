package io.github.kingqiang.cardgame.cardgamebackend.room.service;

import io.github.kingqiang.cardgame.cardgamebackend.bot.BotPlayerRegistry;
import io.github.kingqiang.cardgame.cardgamebackend.common.exception.BusinessException;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import io.github.kingqiang.cardgame.cardgamebackend.room.dto.AddRoomBotRequest;
import io.github.kingqiang.cardgame.cardgamebackend.room.dto.RoomDetailResponse;
import io.github.kingqiang.cardgame.cardgamebackend.room.entity.GameRoom;
import io.github.kingqiang.cardgame.cardgamebackend.room.websocket.GameMessageBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

/**
 * 业务服务：RoomBot 领域逻辑。
 */
@Service
@RequiredArgsConstructor
public class RoomBotService {

    private final BotPlayerRegistry botPlayerRegistry;
    private final RoomService roomService;
    private final GameMessageBroadcaster broadcaster;

    @Transactional
    public RoomDetailResponse addBots(String roomId, long userId, AddRoomBotRequest request) {
        GameRoom room = findJoinableFriendRoom(roomId, userId);
        int count = request != null && request.getCount() > 0 ? request.getCount() : 1;
        if (roomService.countHumans(roomId) < 1) {
            throw new BusinessException(ErrorCode.BOT_NOT_ALLOWED, "房间至少需要一名真人玩家");
        }
        if (roomService.countRobots(roomId) + count > 2) {
            throw new BusinessException(ErrorCode.BOT_NOT_ALLOWED, "最多添加 2 名电脑");
        }

        Set<Long> excluded = roomService.occupiedUserIds(roomId);
        List<Long> botIds = botPlayerRegistry.pickBots(count, excluded);
        if (botIds.size() < count) {
            throw new BusinessException(ErrorCode.BOT_NOT_ALLOWED, "系统电脑玩家不足");
        }

        for (Long botId : botIds) {
            OptionalInt seat = roomService.findNextEmptySeat(roomId, room.getMaxPlayers());
            if (seat.isEmpty()) {
                break;
            }
            roomService.seatPlayer(room, botId, seat.getAsInt(), true, true);
            broadcaster.broadcastRoomEvent(roomId, Map.of(
                    "event", "BOT_JOIN",
                    "userId", botId,
                    "seat", seat.getAsInt()
            ));
        }
        return roomService.getRoom(roomId);
    }

    @Transactional
    public RoomDetailResponse removeBot(String roomId, long userId, int seat) {
        GameRoom room = findJoinableFriendRoom(roomId, userId);
        roomService.removeRobotAtSeat(roomId, seat);
        broadcaster.broadcastRoomEvent(roomId, Map.of(
                "event", "BOT_LEAVE",
                "seat", seat
        ));
        return roomService.getRoom(roomId);
    }

    private GameRoom findJoinableFriendRoom(String roomId, long userId) {
        RoomDetailResponse detail = roomService.getRoom(roomId);
        if (!"FRIEND".equals(detail.getMode())) {
            throw new BusinessException(ErrorCode.BOT_NOT_ALLOWED, "仅亲友房可添加电脑");
        }
        if (!"WAITING".equals(detail.getStatus())) {
            throw new BusinessException(ErrorCode.ROOM_NOT_JOINABLE, "对局已开始，无法调整电脑");
        }
        if (!userIdEquals(detail.getOwnerId(), userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "仅房主可管理电脑");
        }
        return roomService.getRoomEntity(roomId);
    }

    private boolean userIdEquals(Long ownerId, long userId) {
        return ownerId != null && ownerId == userId;
    }
}
