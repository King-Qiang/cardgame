package io.github.kingqiang.cardgame.cardgamebackend.room.service;

import io.github.kingqiang.cardgame.cardgamebackend.common.exception.BusinessException;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.BusinessIdGenerator;
import io.github.kingqiang.cardgame.cardgamebackend.game.context.GameContext;
import io.github.kingqiang.cardgame.cardgamebackend.game.context.GameContextStore;
import io.github.kingqiang.cardgame.cardgamebackend.game.engine.GameEngine;
import io.github.kingqiang.cardgame.cardgamebackend.game.engine.GameEngineRegistry;
import io.github.kingqiang.cardgame.cardgamebackend.room.dto.CreateRoomRequest;
import io.github.kingqiang.cardgame.cardgamebackend.room.dto.RoomDetailResponse;
import io.github.kingqiang.cardgame.cardgamebackend.room.entity.GameRoom;
import io.github.kingqiang.cardgame.cardgamebackend.room.entity.RoomPlayer;
import io.github.kingqiang.cardgame.cardgamebackend.room.repository.GameRoomRepository;
import io.github.kingqiang.cardgame.cardgamebackend.room.repository.RoomPlayerRepository;
import io.github.kingqiang.cardgame.cardgamebackend.room.websocket.GameMessageBroadcaster;
import io.github.kingqiang.cardgame.cardgamebackend.user.entity.Player;
import io.github.kingqiang.cardgame.cardgamebackend.user.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 业务服务：Room 领域逻辑。
 */
@Service
@RequiredArgsConstructor
public class RoomService {

    private final GameRoomRepository gameRoomRepository;
    private final RoomPlayerRepository roomPlayerRepository;
    private final BusinessIdGenerator businessIdGenerator;
    private final GameContextStore gameContextStore;
    private final GameEngineRegistry gameEngineRegistry;
    private final GameSessionService gameSessionService;
    private final GameMessageBroadcaster broadcaster;
    private final PlayerRepository playerRepository;

    @Transactional
    public RoomDetailResponse createMatchRoom(String gameType, String mode, List<Long> playerIds) {
        if (playerIds == null || playerIds.size() < 2) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "匹配玩家不足");
        }
        LocalDateTime now = LocalDateTime.now();
        String roomId = businessIdGenerator.nextRoomId();
        GameRoom room = new GameRoom();
        room.setRoomId(roomId);
        room.setGameType(gameType);
        room.setMode(mode != null && !mode.isBlank() ? mode : "MATCH");
        room.setStatus("WAITING");
        room.setOwnerId(playerIds.get(0));
        room.setMaxPlayers(3);
        room.setConfigJson(Map.of("baseScore", 1));
        room.setCreatedAt(now);
        room.setUpdatedAt(now);
        gameRoomRepository.save(room);

        for (int i = 0; i < playerIds.size(); i++) {
            joinRoomInternal(room, playerIds.get(i), i);
        }
        broadcaster.broadcastRoomEvent(roomId, Map.of("event", "MATCH_FOUND", "players", playerIds));
        return toDetail(room);
    }

    @Transactional
    public RoomDetailResponse createRoom(long userId, CreateRoomRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String roomId = businessIdGenerator.nextRoomId();
        GameRoom room = new GameRoom();
        room.setRoomId(roomId);
        room.setGameType(request.getGameType());
        room.setMode(request.getMode());
        room.setStatus("WAITING");
        room.setOwnerId(userId);
        room.setMaxPlayers(3);
        room.setConfigJson(request.getConfig() != null ? request.getConfig() : Map.of());
        room.setCreatedAt(now);
        room.setUpdatedAt(now);
        gameRoomRepository.save(room);

        joinRoomInternal(room, userId, 0);
        return toDetail(room);
    }

    @Transactional(readOnly = true)
    public RoomDetailResponse getRoom(String roomId) {
        GameRoom room = findRoom(roomId);
        return toDetail(room);
    }

    @Transactional(readOnly = true)
    public GameRoom getRoomEntity(String roomId) {
        return findRoom(roomId);
    }

    @Transactional
    public RoomDetailResponse joinRoom(String roomId, long userId) {
        GameRoom room = findRoom(roomId);
        if (!"WAITING".equals(room.getStatus())) {
            throw new BusinessException(ErrorCode.ROOM_NOT_JOINABLE);
        }
        if (roomPlayerRepository.findByRoomIdAndUserId(roomId, userId).isPresent()) {
            return toDetail(room);
        }
        long count = roomPlayerRepository.countByRoomId(roomId);
        if (count >= room.getMaxPlayers()) {
            throw new BusinessException(ErrorCode.ROOM_FULL);
        }
        joinRoomInternal(room, userId, (int) count);
        broadcaster.broadcastRoomEvent(roomId, Map.of("event", "PLAYER_JOIN", "userId", userId));
        return toDetail(room);
    }

    @Transactional
    public RoomDetailResponse leaveRoom(String roomId, long userId) {
        GameRoom room = findRoom(roomId);
        roomPlayerRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_IN_ROOM));
        if ("PLAYING".equals(room.getStatus())) {
            throw new BusinessException(ErrorCode.ROOM_NOT_JOINABLE, "对局进行中无法离开");
        }
        roomPlayerRepository.deleteByRoomIdAndUserId(roomId, userId);
        GameContext ctx = gameContextStore.getByRoom(roomId);
        if (ctx != null) {
            gameEngineRegistry.get(room.getGameType()).onPlayerLeave(ctx, userId);
            gameContextStore.save(ctx);
        }
        if (room.getOwnerId().equals(userId)) {
            List<RoomPlayer> remaining = roomPlayerRepository.findByRoomIdOrderBySeatAsc(roomId);
            if (!remaining.isEmpty()) {
                room.setOwnerId(remaining.get(0).getUserId());
                room.setUpdatedAt(LocalDateTime.now());
                gameRoomRepository.save(room);
            } else {
                room.setStatus("DISBANDED");
                room.setUpdatedAt(LocalDateTime.now());
                gameRoomRepository.save(room);
                gameContextStore.remove(roomId);
            }
        }
        broadcaster.broadcastRoomEvent(roomId, Map.of("event", "PLAYER_LEAVE", "userId", userId));
        return toDetail(room);
    }

    @Transactional
    public void adminKick(String roomId, long userId, String reason) {
        GameRoom room = findRoom(roomId);
        if ("DISBANDED".equals(room.getStatus())) {
            throw new BusinessException(ErrorCode.ROOM_NOT_JOINABLE, "房间已解散");
        }
        roomPlayerRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_IN_ROOM));
        roomPlayerRepository.deleteByRoomIdAndUserId(roomId, userId);
        GameContext ctx = gameContextStore.getByRoom(roomId);
        if (ctx != null) {
            gameEngineRegistry.get(room.getGameType()).onPlayerLeave(ctx, userId);
            gameContextStore.save(ctx);
        }
        if (room.getOwnerId().equals(userId)) {
            List<RoomPlayer> remaining = roomPlayerRepository.findByRoomIdOrderBySeatAsc(roomId);
            if (!remaining.isEmpty()) {
                room.setOwnerId(remaining.get(0).getUserId());
                room.setUpdatedAt(LocalDateTime.now());
                gameRoomRepository.save(room);
            } else if (!"PLAYING".equals(room.getStatus())) {
                room.setStatus("DISBANDED");
                room.setUpdatedAt(LocalDateTime.now());
                gameRoomRepository.save(room);
                gameContextStore.remove(roomId);
            }
        }
        broadcaster.broadcastRoomEvent(roomId, Map.of(
                "event", "PLAYER_KICK",
                "userId", userId,
                "reason", reason != null ? reason : ""
        ));
    }

    @Transactional
    public RoomDetailResponse setReady(String roomId, long userId, boolean ready) {
        GameRoom room = findRoom(roomId);
        RoomPlayer player = roomPlayerRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_IN_ROOM));
        player.setReady(ready);
        roomPlayerRepository.save(player);
        broadcaster.broadcastRoomEvent(roomId, Map.of("event", "PLAYER_READY", "userId", userId, "ready", ready));
        return toDetail(room);
    }

    @Transactional
    public RoomDetailResponse startRoom(String roomId, long userId) {
        GameRoom room = findRoom(roomId);
        if (!room.getOwnerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "仅房主可开始游戏");
        }
        List<RoomPlayer> players = roomPlayerRepository.findByRoomIdOrderBySeatAsc(roomId);
        if (players.size() < room.getMaxPlayers()) {
            throw new BusinessException(ErrorCode.GAME_NOT_STARTED, "人数未满");
        }
        if (!players.stream().allMatch(RoomPlayer::getReady)) {
            throw new BusinessException(ErrorCode.GAME_NOT_STARTED, "尚有玩家未准备");
        }
        gameSessionService.startGame(room, players);
        return toDetail(findRoom(roomId));
    }

    public void seatPlayer(GameRoom room, long userId, int seat, boolean isRobot, boolean ready) {
        joinRoomInternal(room, userId, seat, isRobot, ready);
    }

    public OptionalInt findNextEmptySeat(String roomId, int maxPlayers) {
        List<RoomPlayer> players = roomPlayerRepository.findByRoomIdOrderBySeatAsc(roomId);
        boolean[] taken = new boolean[maxPlayers];
        for (RoomPlayer player : players) {
            if (player.getSeat() != null && player.getSeat() >= 0 && player.getSeat() < maxPlayers) {
                taken[player.getSeat()] = true;
            }
        }
        for (int seat = 0; seat < maxPlayers; seat++) {
            if (!taken[seat]) {
                return OptionalInt.of(seat);
            }
        }
        return OptionalInt.empty();
    }

    public long countRobots(String roomId) {
        return roomPlayerRepository.findByRoomIdOrderBySeatAsc(roomId).stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsRobot()))
                .count();
    }

    public long countHumans(String roomId) {
        return roomPlayerRepository.findByRoomIdOrderBySeatAsc(roomId).stream()
                .filter(p -> !Boolean.TRUE.equals(p.getIsRobot()))
                .count();
    }

    public Set<Long> occupiedUserIds(String roomId) {
        return roomPlayerRepository.findByRoomIdOrderBySeatAsc(roomId).stream()
                .map(RoomPlayer::getUserId)
                .collect(Collectors.toSet());
    }

    public void removeRobotAtSeat(String roomId, int seat) {
        RoomPlayer player = roomPlayerRepository.findByRoomIdAndSeat(roomId, seat)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR, "该座位无玩家"));
        if (!Boolean.TRUE.equals(player.getIsRobot())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "只能移除电脑玩家");
        }
        GameRoom room = findRoom(roomId);
        roomPlayerRepository.deleteByRoomIdAndUserId(roomId, player.getUserId());
        GameContext ctx = gameContextStore.getByRoom(roomId);
        if (ctx != null) {
            gameEngineRegistry.get(room.getGameType()).onPlayerLeave(ctx, player.getUserId());
            gameContextStore.save(ctx);
        }
    }

    private void joinRoomInternal(GameRoom room, long userId, int seat) {
        joinRoomInternal(room, userId, seat, false, false);
    }

    private void joinRoomInternal(GameRoom room, long userId, int seat, boolean isRobot, boolean ready) {
        LocalDateTime now = LocalDateTime.now();
        RoomPlayer player = new RoomPlayer();
        player.setRoomId(room.getRoomId());
        player.setUserId(userId);
        player.setSeat(seat);
        player.setReady(ready);
        player.setIsRobot(isRobot);
        player.setJoinedAt(now);
        roomPlayerRepository.save(player);

        GameContext ctx = gameContextStore.getByRoom(room.getRoomId());
        if (ctx == null) {
            ctx = new GameContext();
            ctx.setRoomId(room.getRoomId());
            ctx.setGameType(room.getGameType());
            Object baseScore = room.getConfigJson() != null ? room.getConfigJson().get("baseScore") : null;
            if (baseScore instanceof Number number) {
                ctx.setBaseScore(number.intValue());
            }
        }
        GameEngine engine = gameEngineRegistry.get(room.getGameType());
        engine.onPlayerJoin(ctx, userId, seat);
        gameContextStore.save(ctx);
    }

    private GameRoom findRoom(String roomId) {
        return gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
    }

    public RoomDetailResponse toDetail(GameRoom room) {
        List<RoomPlayer> roomPlayers = roomPlayerRepository.findByRoomIdOrderBySeatAsc(room.getRoomId());
        Map<Long, Player> playerById = loadPlayers(roomPlayers);
        List<RoomDetailResponse.RoomPlayerDto> players = roomPlayers.stream()
                .map(p -> toPlayerDto(p, playerById.get(p.getUserId())))
                .toList();
        return RoomDetailResponse.builder()
                .roomId(room.getRoomId())
                .gameType(room.getGameType())
                .mode(room.getMode())
                .status(room.getStatus())
                .ownerId(room.getOwnerId())
                .maxPlayers(room.getMaxPlayers())
                .config(room.getConfigJson() != null ? room.getConfigJson() : Map.of())
                .players(players)
                .createdAt(room.getCreatedAt())
                .build();
    }

    private Map<Long, Player> loadPlayers(List<RoomPlayer> roomPlayers) {
        List<Long> userIds = roomPlayers.stream().map(RoomPlayer::getUserId).distinct().toList();
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return playerRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(Player::getId, Function.identity()));
    }

    private RoomDetailResponse.RoomPlayerDto toPlayerDto(RoomPlayer membership, Player player) {
        String nickname = player != null && player.getNickname() != null && !player.getNickname().isBlank()
                ? player.getNickname()
                : "玩家" + membership.getUserId();
        String avatar = player != null && player.getAvatar() != null ? player.getAvatar() : "";
        return RoomDetailResponse.RoomPlayerDto.builder()
                .userId(membership.getUserId())
                .nickname(nickname)
                .avatar(avatar)
                .seat(membership.getSeat())
                .ready(Boolean.TRUE.equals(membership.getReady()))
                .isRobot(Boolean.TRUE.equals(membership.getIsRobot()))
                .build();
    }
}
