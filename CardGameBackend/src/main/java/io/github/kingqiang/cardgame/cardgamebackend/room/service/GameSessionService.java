package io.github.kingqiang.cardgame.cardgamebackend.room.service;

import io.github.kingqiang.cardgame.cardgamebackend.bot.BotPlayerRegistry;
import io.github.kingqiang.cardgame.cardgamebackend.bot.BotTurnScheduler;
import io.github.kingqiang.cardgame.cardgamebackend.common.exception.BusinessException;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.BusinessIdGenerator;
import io.github.kingqiang.cardgame.cardgamebackend.game.context.GameContext;
import io.github.kingqiang.cardgame.cardgamebackend.game.context.GameContextStore;
import io.github.kingqiang.cardgame.cardgamebackend.game.engine.ActionResult;
import io.github.kingqiang.cardgame.cardgamebackend.game.engine.GameAction;
import io.github.kingqiang.cardgame.cardgamebackend.game.engine.GameEngine;
import io.github.kingqiang.cardgame.cardgamebackend.game.engine.GameEngineRegistry;
import io.github.kingqiang.cardgame.cardgamebackend.game.engine.SettlementItem;
import io.github.kingqiang.cardgame.cardgamebackend.record.entity.GameActionLog;
import io.github.kingqiang.cardgame.cardgamebackend.record.entity.GameRecord;
import io.github.kingqiang.cardgame.cardgamebackend.record.repository.GameActionLogRepository;
import io.github.kingqiang.cardgame.cardgamebackend.record.repository.GameRecordRepository;
import io.github.kingqiang.cardgame.cardgamebackend.room.entity.GameRoom;
import io.github.kingqiang.cardgame.cardgamebackend.room.entity.RoomPlayer;
import io.github.kingqiang.cardgame.cardgamebackend.room.repository.GameRoomRepository;
import io.github.kingqiang.cardgame.cardgamebackend.room.websocket.GameMessageBroadcaster;
import io.github.kingqiang.cardgame.cardgamebackend.settlement.dto.SettlementEvent;
import io.github.kingqiang.cardgame.cardgamebackend.settlement.service.SettlementService;
import io.github.kingqiang.cardgame.cardgamebackend.user.SystemPlayers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 对局会话编排：开局、玩家/Bot 行动、action 落库、STATE_SYNC/SETTLEMENT 广播与结算触发。
 */
@Service
@RequiredArgsConstructor
public class GameSessionService {

    private final GameRecordRepository gameRecordRepository;
    private final GameActionLogRepository gameActionLogRepository;
    private final GameRoomRepository gameRoomRepository;
    private final BusinessIdGenerator businessIdGenerator;
    private final GameContextStore gameContextStore;
    private final GameEngineRegistry gameEngineRegistry;
    private final GameMessageBroadcaster broadcaster;
    private final SettlementService settlementService;
    private final BotTurnScheduler botTurnScheduler;
    private final BotPlayerRegistry botPlayerRegistry;

    @Transactional
    public void startGame(GameRoom room, List<RoomPlayer> players) {
        GameContext ctx = gameContextStore.getByRoom(room.getRoomId());
        if (ctx == null) {
            throw new BusinessException(ErrorCode.GAME_NOT_STARTED);
        }
        GameEngine engine = gameEngineRegistry.get(room.getGameType());
        if (!engine.canStart(ctx)) {
            throw new BusinessException(ErrorCode.GAME_NOT_STARTED, "开局条件未满足");
        }

        String recordId = businessIdGenerator.nextRecordId();
        LocalDateTime now = LocalDateTime.now();
        GameRecord record = new GameRecord();
        record.setRecordId(recordId);
        record.setRoomId(room.getRoomId());
        record.setGameType(room.getGameType());
        record.setStatus("PLAYING");
        record.setStartAt(now);
        gameRecordRepository.save(record);

        ctx.setRecordId(recordId);
        engine.onGameStart(ctx);
        ctx.setActionSeq(0);
        gameContextStore.save(ctx);

        logSystemAction(ctx, SystemPlayers.SYSTEM_ACTOR_ID, "GAME_START", Map.of(
                "players", ctx.getSeatUser().entrySet().stream()
                        .collect(Collectors.toMap(e -> String.valueOf(e.getKey()), Map.Entry::getValue)),
                "seats", ctx.getSeatUser().keySet().stream().sorted().toList()
        ));
        logSystemAction(ctx, SystemPlayers.SYSTEM_ACTOR_ID, "DEAL", Map.of(
                "handCounts", ctx.getHands().entrySet().stream()
                        .collect(Collectors.toMap(e -> String.valueOf(e.getKey()), e -> e.getValue().size()))
        ));

        room.setStatus("PLAYING");
        room.setUpdatedAt(now);
        gameRoomRepository.save(room);

        broadcaster.broadcastStateSync(room.getRoomId(), ctx);
        botTurnScheduler.schedule(room.getRoomId());
    }

    @Transactional
    public ActionResult handleAction(String roomId, long userId, int clientSeq, GameAction action) {
        GameContext ctx = gameContextStore.getByRoom(roomId);
        if (ctx == null || ctx.getRecordId() == null) {
            throw new BusinessException(ErrorCode.GAME_NOT_STARTED);
        }
        if (clientSeq <= ctx.getActionSeq()) {
            throw new BusinessException(ErrorCode.GAME_ACTION_INVALID, "重复或乱序操作");
        }

        String phaseBefore = ctx.getPhase();
        GameEngine engine = gameEngineRegistry.get(ctx.getGameType());
        ActionResult result = engine.handleAction(ctx, action);
        if (!result.isSuccess()) {
            broadcaster.broadcastActionResult(roomId, false, result.getMessage(), userId);
            return result;
        }

        ctx.setActionSeq(clientSeq);
        gameContextStore.save(ctx);
        persistAction(ctx, userId, action, clientSeq, phaseBefore);

        if ("BIDDING".equals(phaseBefore) && "PLAYING".equals(ctx.getPhase())) {
            int landlordSeat = ctx.getLandlordSeat();
            Map<String, Object> payload = new HashMap<>();
            payload.put("seat", landlordSeat);
            payload.put("bottomCards", ctx.getBottomCards());
            logSystemAction(ctx, ctx.userIdOfSeat(landlordSeat), "LANDLORD", payload);
        }

        broadcaster.broadcastActionResult(roomId, true, "ok", userId);
        broadcaster.broadcastStateSync(roomId, ctx);

        if (result.isGameFinished()) {
            finishGame(ctx, engine);
        } else {
            botTurnScheduler.schedule(roomId);
        }
        return result;
    }

    public Object getVisibleState(String roomId, long userId) {
        GameContext ctx = gameContextStore.getByRoom(roomId);
        if (ctx == null) {
            throw new BusinessException(ErrorCode.GAME_NOT_STARTED);
        }
        GameEngine engine = gameEngineRegistry.get(ctx.getGameType());
        return engine.getVisibleState(ctx, userId);
    }

    @Transactional
    public void reconnect(String roomId, long userId) {
        GameContext ctx = gameContextStore.getByRoom(roomId);
        if (ctx == null) {
            throw new BusinessException(ErrorCode.NOT_IN_ROOM);
        }
        broadcaster.sendStateSyncToUser(roomId, userId, ctx);
    }

    private void finishGame(GameContext ctx, GameEngine engine) {
        List<SettlementItem> settlements = engine.settle(ctx);
        LocalDateTime now = LocalDateTime.now();

        GameRecord record = gameRecordRepository.findById(ctx.getRecordId()).orElseThrow();
        GameRoom room = gameRoomRepository.findById(ctx.getRoomId()).orElseThrow();
        Map<String, Object> result = new HashMap<>();
        Integer winnerSeat = ctx.getWinnerSeat() != null ? ctx.getWinnerSeat() : ctx.getLandlordSeat();
        result.put("winnerSeat", winnerSeat);
        result.put("landlordSeat", ctx.getLandlordSeat());
        result.put("multiplier", ctx.getMultiplier());
        result.put("mode", room.getMode());
        result.put("participants", settlements.stream()
                .map(item -> {
                    Map<String, Object> participant = new HashMap<>();
                    participant.put("userId", item.getUserId());
                    participant.put("seat", item.getSeat());
                    boolean isRobot = botPlayerRegistry.isBot(item.getUserId());
                    participant.put("isRobot", isRobot);
                    participant.put("goldDelta", isRobot ? 0 : item.getGoldDelta());
                    participant.put("scoreDelta", isRobot ? 0 : item.getScoreDelta());
                    return participant;
                })
                .toList());
        record.setStatus("FINISHED");
        record.setEndAt(now);
        record.setResultJson(result);
        gameRecordRepository.save(record);

        Map<String, Object> settlementPayload = new HashMap<>();
        settlementPayload.put("winners", List.of(ctx.getWinnerSeat()));
        settlementPayload.put("goldDelta", settlements.stream()
                .collect(Collectors.toMap(s -> String.valueOf(s.getUserId()), SettlementItem::getGoldDelta)));
        logSystemAction(ctx, SystemPlayers.SYSTEM_ACTOR_ID, "SETTLEMENT", settlementPayload);

        SettlementEvent event = settlementService.buildEvent(
                ctx.getRecordId(), ctx.getRoomId(), room.getGameType(), room.getMode(), ctx, settlements);
        settlementService.dispatch(event, ctx);

        room.setStatus("WAITING");
        room.setUpdatedAt(now);
        gameRoomRepository.save(room);

        broadcaster.broadcastSettlement(ctx.getRoomId(), settlements, result);
        ctx.setPhase("FINISHED");
        gameContextStore.save(ctx);
    }

    private void persistAction(GameContext ctx, long userId, GameAction action, int seq, String phaseBefore) {
        int seat = ctx.seatOfUser(userId);
        String actionType = action.getAction();
        Map<String, Object> payload = new HashMap<>();
        payload.put("seat", seat);

        if ("BIDDING".equals(phaseBefore)) {
            if ("CALL_LANDLORD".equals(actionType)) {
                actionType = "BID";
                payload.put("score", 1);
                payload.put("passed", false);
            } else if ("PASS".equals(actionType)) {
                actionType = "BID";
                payload.put("passed", true);
            }
        } else if ("PLAYING".equals(phaseBefore)) {
            if ("PLAY_CARDS".equals(actionType) && action.getCards() != null) {
                payload.put("cards", action.getCards());
                List<String> hand = ctx.getHands().get(seat);
                payload.put("handRemaining", hand != null ? hand.size() : 0);
            }
        }

        saveActionLog(ctx.getRecordId(), seq, userId, actionType, payload);
    }

    private void logSystemAction(GameContext ctx, long userId, String actionType, Map<String, Object> payload) {
        int seq = nextSystemSeq(ctx);
        saveActionLog(ctx.getRecordId(), seq, userId, actionType, payload);
    }

    private int nextSystemSeq(GameContext ctx) {
        int seq = ctx.getActionSeq() + 1;
        ctx.setActionSeq(seq);
        return seq;
    }

    private void saveActionLog(String recordId, int seq, long userId, String actionType, Map<String, Object> payload) {
        GameActionLog log = new GameActionLog();
        log.setRecordId(recordId);
        log.setSeq(seq);
        log.setUserId(userId);
        log.setAction(actionType);
        log.setPayload(payload.isEmpty() ? null : payload);
        log.setCreatedAt(LocalDateTime.now());
        gameActionLogRepository.save(log);
    }
}
