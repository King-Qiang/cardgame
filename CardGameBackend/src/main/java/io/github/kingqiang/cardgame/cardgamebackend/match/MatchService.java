package io.github.kingqiang.cardgame.cardgamebackend.match;

import io.github.kingqiang.cardgame.cardgamebackend.auth.security.SecurityUtils;
import io.github.kingqiang.cardgame.cardgamebackend.common.exception.BusinessException;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import io.github.kingqiang.cardgame.cardgamebackend.match.dto.QuickMatchRequest;
import io.github.kingqiang.cardgame.cardgamebackend.match.dto.QuickMatchResponse;
import io.github.kingqiang.cardgame.cardgamebackend.rank.RankMatchHelper;
import io.github.kingqiang.cardgame.cardgamebackend.rank.service.RankService;
import io.github.kingqiang.cardgame.cardgamebackend.room.dto.RoomDetailResponse;
import io.github.kingqiang.cardgame.cardgamebackend.room.entity.GameRoom;
import io.github.kingqiang.cardgame.cardgamebackend.room.entity.RoomPlayer;
import io.github.kingqiang.cardgame.cardgamebackend.room.repository.GameRoomRepository;
import io.github.kingqiang.cardgame.cardgamebackend.room.repository.RoomPlayerRepository;
import io.github.kingqiang.cardgame.cardgamebackend.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 快速匹配：入队、凑满 3 人建房并返回 roomId。
 */
@Service
@RequiredArgsConstructor
public class MatchService {

    private static final int REQUIRED_PLAYERS = 3;

    private final MatchQueue matchQueue;
    private final MatchResultStore matchResultStore;
    private final RoomService roomService;
    private final RankService rankService;
    private final RoomPlayerRepository roomPlayerRepository;
    private final GameRoomRepository gameRoomRepository;

    @Transactional
    public QuickMatchResponse quickMatch(QuickMatchRequest request) {
        long userId = SecurityUtils.requirePlayerId();
        String gameType = request.getGameType();
        String mode = normalizeMode(request.getMode());

        if (matchQueue.isQueued(userId)) {
            throw new BusinessException(ErrorCode.MATCH_ALREADY_IN_QUEUE);
        }

        String queueKey = resolveQueueKey(userId, gameType, mode);
        matchQueue.enqueue(queueKey, userId);

        Optional<List<Long>> matched = tryPoll(gameType, mode, queueKey, userId);
        return matched.map(players -> createMatchedRoom(gameType, mode, players, queueKey))
                .orElseGet(() -> waitingResponse(gameType, mode, queueKey, userId));
    }

    @Transactional(readOnly = true)
    public QuickMatchResponse getMatchStatus(String gameType) {
        long userId = SecurityUtils.requirePlayerId();

        Optional<MatchResultStore.Entry> pending = matchResultStore.get(userId, gameType);
        if (pending.isPresent()) {
            MatchResultStore.Entry entry = pending.get();
            return QuickMatchResponse.builder()
                    .status("MATCHED")
                    .roomId(entry.roomId())
                    .queueSize(0)
                    .requiredPlayers(REQUIRED_PLAYERS)
                    .matchMode(entry.matchMode())
                    .matchTier(entry.matchTier())
                    .build();
        }

        if (!matchQueue.isQueued(userId)) {
            Optional<String> waitingRoom = findWaitingMatchRoom(userId, gameType);
            if (waitingRoom.isPresent()) {
                return QuickMatchResponse.builder()
                        .status("MATCHED")
                        .roomId(waitingRoom.get())
                        .queueSize(0)
                        .requiredPlayers(REQUIRED_PLAYERS)
                        .build();
            }
            return QuickMatchResponse.builder()
                    .status("NOT_IN_QUEUE")
                    .queueSize(0)
                    .requiredPlayers(REQUIRED_PLAYERS)
                    .build();
        }

        String queueKey = matchQueue.getQueueKey(userId);
        if (queueKey == null || !queueKey.startsWith(gameType)) {
            return QuickMatchResponse.builder()
                    .status("NOT_IN_QUEUE")
                    .queueSize(0)
                    .requiredPlayers(REQUIRED_PLAYERS)
                    .build();
        }

        String mode = queueKey.contains(":") ? "RANKED" : "MATCH";
        long waitMs = System.currentTimeMillis() - matchQueue.getEnqueueTime(userId);
        String matchTier = "RANKED".equals(mode) ? queueKey.substring(gameType.length() + 1) : null;
        return QuickMatchResponse.builder()
                .status("WAITING")
                .queueSize(matchQueue.queueSize(queueKey))
                .requiredPlayers(REQUIRED_PLAYERS)
                .matchMode(mode)
                .matchTier(matchTier)
                .estimatedWaitSec("RANKED".equals(mode) ? RankMatchHelper.estimatedWaitSec(waitMs) : null)
                .build();
    }

    public QuickMatchResponse cancelQuickMatch(String gameType) {
        long userId = SecurityUtils.requirePlayerId();
        if (!matchQueue.isQueued(userId)) {
            throw new BusinessException(ErrorCode.MATCH_NOT_IN_QUEUE);
        }
        String queueKey = matchQueue.getQueueKey(userId);
        matchQueue.cancel(userId);
        matchResultStore.remove(userId);
        return QuickMatchResponse.builder()
                .status("CANCELLED")
                .queueSize(queueKey != null ? matchQueue.queueSize(queueKey) : 0)
                .requiredPlayers(REQUIRED_PLAYERS)
                .matchMode(queueKey != null && queueKey.contains(":") ? "RANKED" : "MATCH")
                .build();
    }

    private Optional<List<Long>> tryPoll(String gameType, String mode, String queueKey, long userId) {
        if ("RANKED".equals(mode)) {
            String tier = queueKey.substring(gameType.length() + 1);
            long waitMs = System.currentTimeMillis() - matchQueue.getEnqueueTime(userId);
            List<String> keys = RankMatchHelper.queueKeysForMatch(gameType, tier, waitMs);
            return matchQueue.pollMatchedPlayersFromKeys(keys, REQUIRED_PLAYERS);
        }
        return matchQueue.pollMatchedPlayers(queueKey, REQUIRED_PLAYERS);
    }

    private QuickMatchResponse createMatchedRoom(String gameType, String mode, List<Long> players, String queueKey) {
        RoomDetailResponse room = roomService.createMatchRoom(gameType, mode, players);
        String matchTier = "RANKED".equals(mode) ? queueKey.substring(gameType.length() + 1) : null;
        matchResultStore.save(gameType, room.getRoomId(), mode, matchTier, players);
        return QuickMatchResponse.builder()
                .status("MATCHED")
                .roomId(room.getRoomId())
                .queueSize(0)
                .requiredPlayers(REQUIRED_PLAYERS)
                .matchMode(mode)
                .matchTier(matchTier)
                .build();
    }

    private QuickMatchResponse waitingResponse(String gameType, String mode, String queueKey, long userId) {
        long waitMs = System.currentTimeMillis() - matchQueue.getEnqueueTime(userId);
        String matchTier = "RANKED".equals(mode) ? queueKey.substring(gameType.length() + 1) : null;
        return QuickMatchResponse.builder()
                .status("WAITING")
                .queueSize(matchQueue.queueSize(queueKey))
                .requiredPlayers(REQUIRED_PLAYERS)
                .matchMode(mode)
                .matchTier(matchTier)
                .estimatedWaitSec("RANKED".equals(mode) ? RankMatchHelper.estimatedWaitSec(waitMs) : null)
                .build();
    }

    private String resolveQueueKey(long userId, String gameType, String mode) {
        if ("RANKED".equals(mode)) {
            String tier = rankService.getTier(userId, gameType);
            return RankMatchHelper.queueKey(gameType, tier);
        }
        return gameType;
    }

    private String normalizeMode(String mode) {
        if (mode == null || mode.isBlank()) {
            return "MATCH";
        }
        return "RANKED".equalsIgnoreCase(mode) ? "RANKED" : "MATCH";
    }

    private Optional<String> findWaitingMatchRoom(long userId, String gameType) {
        for (RoomPlayer membership : roomPlayerRepository.findByUserId(userId)) {
            Optional<GameRoom> roomOpt = gameRoomRepository.findById(membership.getRoomId());
            if (roomOpt.isEmpty()) {
                continue;
            }
            GameRoom room = roomOpt.get();
            if (!gameType.equals(room.getGameType())) {
                continue;
            }
            if (!"WAITING".equals(room.getStatus())) {
                continue;
            }
            String mode = room.getMode();
            if ("MATCH".equals(mode) || "RANKED".equals(mode)) {
                return Optional.of(room.getRoomId());
            }
        }
        return Optional.empty();
    }

    /**
     * 供 MatchExpandJob 调用：为等待超过阈值的 RANKED 玩家扩大段位范围并尝试撮合。
     */
    public int tryExpandRankedMatches() {
        int created = 0;
        String gameType = "DOUDIZHU";
        for (String tier : List.of("BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND")) {
            List<String> keys = RankMatchHelper.queueKeysForMatch(gameType, tier, 35_000L);
            Optional<List<Long>> matched;
            while ((matched = matchQueue.pollMatchedPlayersFromKeys(keys, REQUIRED_PLAYERS)).isPresent()) {
                createMatchedRoom(gameType, "RANKED", matched.get(), RankMatchHelper.queueKey(gameType, tier));
                created++;
            }
        }
        return created;
    }
}
