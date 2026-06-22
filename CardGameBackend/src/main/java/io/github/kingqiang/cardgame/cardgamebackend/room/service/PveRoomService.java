package io.github.kingqiang.cardgame.cardgamebackend.room.service;

import io.github.kingqiang.cardgame.cardgamebackend.bot.BotPlayerRegistry;
import io.github.kingqiang.cardgame.cardgamebackend.bot.BotConfigService;
import io.github.kingqiang.cardgame.cardgamebackend.bot.BotDifficulty;
import io.github.kingqiang.cardgame.cardgamebackend.common.exception.BusinessException;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.BusinessIdGenerator;
import io.github.kingqiang.cardgame.cardgamebackend.room.dto.CreatePveRoomRequest;
import io.github.kingqiang.cardgame.cardgamebackend.room.dto.RoomDetailResponse;
import io.github.kingqiang.cardgame.cardgamebackend.room.entity.GameRoom;
import io.github.kingqiang.cardgame.cardgamebackend.room.entity.RoomPlayer;
import io.github.kingqiang.cardgame.cardgamebackend.room.repository.GameRoomRepository;
import io.github.kingqiang.cardgame.cardgamebackend.room.repository.RoomPlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 人机练习房间：创建 PVE 房间、Bot 入座并同步开局。
 */
@Service
@RequiredArgsConstructor
public class PveRoomService {

    private static final int REQUIRED_PLAYERS = 3;

    private final BotConfigService botConfigService;
    private final BotPlayerRegistry botPlayerRegistry;
    private final BusinessIdGenerator businessIdGenerator;
    private final GameRoomRepository gameRoomRepository;
    private final RoomPlayerRepository roomPlayerRepository;
    private final RoomService roomService;
    private final GameSessionService gameSessionService;

    @Transactional
    public RoomDetailResponse createPveRoom(long userId, CreatePveRoomRequest request) {
        if (!botConfigService.isPveEnabled()) {
            throw new BusinessException(ErrorCode.PVE_DISABLED);
        }
        if (request.getGameType() == null || !"DOUDIZHU".equalsIgnoreCase(request.getGameType())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "人机练习暂仅支持斗地主");
        }

        BotDifficulty difficulty = botConfigService.defaultDifficulty();
        Map<String, Object> config = new HashMap<>();
        config.put("baseScore", 1);
        config.put("botDifficulty", difficulty.name());
        if (request.getConfig() != null) {
            config.putAll(request.getConfig());
            if (request.getConfig().get("botDifficulty") != null) {
                difficulty = BotDifficulty.fromString(String.valueOf(request.getConfig().get("botDifficulty")));
                config.put("botDifficulty", difficulty.name());
            }
        }

        LocalDateTime now = LocalDateTime.now();
        String roomId = businessIdGenerator.nextRoomId();
        GameRoom room = new GameRoom();
        room.setRoomId(roomId);
        room.setGameType("DOUDIZHU");
        room.setMode("PVE");
        room.setStatus("WAITING");
        room.setOwnerId(userId);
        room.setMaxPlayers(REQUIRED_PLAYERS);
        room.setConfigJson(config);
        room.setCreatedAt(now);
        room.setUpdatedAt(now);
        gameRoomRepository.save(room);

        roomService.seatPlayer(room, userId, 0, false, true);

        Set<Long> excluded = roomService.occupiedUserIds(roomId);
        List<Long> botIds = botPlayerRegistry.pickBots(2, excluded);
        if (botIds.size() < 2) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "系统电脑玩家不足");
        }
        roomService.seatPlayer(room, botIds.get(0), 1, true, true);
        roomService.seatPlayer(room, botIds.get(1), 2, true, true);

        List<RoomPlayer> players = roomPlayerRepository.findByRoomIdOrderBySeatAsc(roomId);
        gameSessionService.startGame(room, players);
        return roomService.getRoom(roomId);
    }
}
