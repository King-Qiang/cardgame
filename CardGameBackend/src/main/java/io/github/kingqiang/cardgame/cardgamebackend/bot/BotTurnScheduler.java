package io.github.kingqiang.cardgame.cardgamebackend.bot;

import io.github.kingqiang.cardgame.cardgamebackend.game.context.GameContext;
import io.github.kingqiang.cardgame.cardgamebackend.game.context.GameContextStore;
import io.github.kingqiang.cardgame.cardgamebackend.game.engine.GameAction;
import io.github.kingqiang.cardgame.cardgamebackend.room.entity.GameRoom;
import io.github.kingqiang.cardgame.cardgamebackend.room.entity.RoomPlayer;
import io.github.kingqiang.cardgame.cardgamebackend.room.repository.GameRoomRepository;
import io.github.kingqiang.cardgame.cardgamebackend.room.repository.RoomPlayerRepository;
import io.github.kingqiang.cardgame.cardgamebackend.room.service.GameSessionService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Bot 回合调度：延迟后代 Bot 调用 handleAction，链式驱动至真人回合。
 */
@Slf4j
@Component
public class BotTurnScheduler {

    private final ObjectProvider<GameSessionService> gameSessionService;
    private final GameContextStore gameContextStore;
    private final GameRoomRepository gameRoomRepository;
    private final RoomPlayerRepository roomPlayerRepository;
    private final BotConfigService botConfigService;
    private final DoudizhuBotRegistry doudizhuBotRegistry;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "bot-turn-scheduler");
        thread.setDaemon(true);
        return thread;
    });
    private final Map<String, ScheduledFuture<?>> pending = new ConcurrentHashMap<>();

    public BotTurnScheduler(ObjectProvider<GameSessionService> gameSessionService,
                            GameContextStore gameContextStore,
                            GameRoomRepository gameRoomRepository,
                            RoomPlayerRepository roomPlayerRepository,
                            BotConfigService botConfigService,
                            DoudizhuBotRegistry doudizhuBotRegistry) {
        this.gameSessionService = gameSessionService;
        this.gameContextStore = gameContextStore;
        this.gameRoomRepository = gameRoomRepository;
        this.roomPlayerRepository = roomPlayerRepository;
        this.botConfigService = botConfigService;
        this.doudizhuBotRegistry = doudizhuBotRegistry;
    }

    public void schedule(String roomId) {
        if (roomId == null || roomId.isBlank()) {
            return;
        }
        GameRoom room = gameRoomRepository.findById(roomId).orElse(null);
        if (room == null || !"PLAYING".equals(room.getStatus())) {
            return;
        }
        long delayMs = botConfigService.resolveActionDelayMs(room);
        pending.compute(roomId, (id, existing) -> {
            if (existing != null) {
                existing.cancel(false);
            }
            return executor.schedule(() -> runBotTurn(roomId), delayMs, TimeUnit.MILLISECONDS);
        });
    }

    private void runBotTurn(String roomId) {
        pending.remove(roomId);
        try {
            GameContext ctx = gameContextStore.getByRoom(roomId);
            if (ctx == null || ctx.getRecordId() == null || "FINISHED".equals(ctx.getPhase())) {
                return;
            }
            int seat = ctx.getCurrentSeat();
            RoomPlayer current = findPlayerBySeat(roomId, seat);
            if (current == null || !Boolean.TRUE.equals(current.getIsRobot())) {
                return;
            }
            GameRoom room = gameRoomRepository.findById(roomId).orElse(null);
            if (room == null) {
                return;
            }
            BotDifficulty difficulty = botConfigService.resolveDifficulty(room);
            GameAction action = doudizhuBotRegistry.get(difficulty).decide(ctx, seat, difficulty);
            int nextSeq = ctx.getActionSeq() + 1;
            gameSessionService.getObject().handleAction(roomId, current.getUserId(), nextSeq, action);
        } catch (Exception ex) {
            log.warn("Bot turn failed for room {}: {}", roomId, ex.getMessage());
        }
    }

    private RoomPlayer findPlayerBySeat(String roomId, int seat) {
        List<RoomPlayer> players = roomPlayerRepository.findByRoomIdOrderBySeatAsc(roomId);
        for (RoomPlayer player : players) {
            if (player.getSeat() != null && player.getSeat() == seat) {
                return player;
            }
        }
        return null;
    }

    @PreDestroy
    public void shutdown() {
        pending.values().forEach(future -> future.cancel(false));
        pending.clear();
        executor.shutdownNow();
    }
}
