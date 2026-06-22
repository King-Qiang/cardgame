package io.github.kingqiang.cardgame.cardgamebackend.room.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kingqiang.cardgame.cardgamebackend.bot.BotPlayerRegistry;
import io.github.kingqiang.cardgame.cardgamebackend.game.context.GameContext;
import io.github.kingqiang.cardgame.cardgamebackend.game.engine.GameEngine;
import io.github.kingqiang.cardgame.cardgamebackend.game.engine.GameEngineRegistry;
import io.github.kingqiang.cardgame.cardgamebackend.game.engine.SettlementItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 对局消息广播：STATE_SYNC、ACTION_RESULT、SETTLEMENT（显式 Map 序列化）。
 */
@Component
@RequiredArgsConstructor
public class GameMessageBroadcaster {

    private final WebSocketSessionManager sessionManager;
    private final GameEngineRegistry gameEngineRegistry;
    private final ObjectMapper objectMapper;
    private final BotPlayerRegistry botPlayerRegistry;
    private final org.springframework.beans.factory.ObjectProvider<RedisWebSocketRelay> redisRelay;

    public void broadcastStateSync(String roomId, GameContext ctx) {
        GameEngine engine = gameEngineRegistry.get(ctx.getGameType());
        for (Long userId : sessionManager.userIdsInRoom(roomId)) {
            Object payload = engine.getVisibleState(ctx, userId);
            send(userId, message("STATE_SYNC", roomId, ctx.getActionSeq(), payload));
        }
    }

    public void sendStateSyncToUser(String roomId, long userId, GameContext ctx) {
        GameEngine engine = gameEngineRegistry.get(ctx.getGameType());
        Object payload = engine.getVisibleState(ctx, userId);
        send(userId, message("STATE_SYNC", roomId, ctx.getActionSeq(), payload));
    }

    public void broadcastActionResult(String roomId, boolean success, String message, long userId) {
        Map<String, Object> payload = Map.of("success", success, "message", message, "userId", userId);
        broadcastRoom(roomId, message("ACTION_RESULT", roomId, null, payload));
    }

    public void broadcastRoomEvent(String roomId, Map<String, Object> payload) {
        broadcastRoom(roomId, message("ROOM_EVENT", roomId, null, payload));
    }

    public void broadcastSettlement(String roomId, List<SettlementItem> settlements, Map<String, Object> result) {
        List<Map<String, Object>> lines = settlements.stream()
                .map(item -> {
                    boolean isRobot = botPlayerRegistry.isBot(item.getUserId());
                    Map<String, Object> line = new HashMap<>();
                    line.put("userId", item.getUserId());
                    line.put("seat", item.getSeat());
                    line.put("scoreDelta", isRobot ? 0 : item.getScoreDelta());
                    line.put("goldDelta", isRobot ? 0 : item.getGoldDelta());
                    line.put("multiplier", item.getMultiplier());
                    return line;
                })
                .collect(Collectors.toList());
        Map<String, Object> payload = Map.of("settlements", lines, "result", result);
        broadcastRoom(roomId, message("SETTLEMENT", roomId, null, payload));
    }

    public void sendPong(long userId) {
        send(userId, message("PONG", null, null, Map.of()));
    }

    public void sendError(long userId, String message) {
        send(userId, message("ERROR", null, null, Map.of("message", message)));
    }

    private void broadcastRoom(String roomId, Map<String, Object> msg) {
        RedisWebSocketRelay relay = redisRelay.getIfAvailable();
        if (relay != null) {
            relay.publish(roomId, msg);
        } else {
            deliverLocalRoomMessage(roomId, msg);
        }
    }

    public void deliverLocalRoomMessage(String roomId, Map<String, Object> msg) {
        for (Long userId : sessionManager.userIdsInRoom(roomId)) {
            send(userId, msg);
        }
    }

    private void send(long userId, Map<String, Object> msg) {
        WebSocketSession session = sessionManager.getSession(userId);
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
        } catch (Exception ignored) {
            // ignore send failure
        }
    }

    private Map<String, Object> message(String type, String roomId, Integer seq, Object payload) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", type);
        if (roomId != null) {
            msg.put("roomId", roomId);
        }
        if (seq != null) {
            msg.put("seq", seq);
        }
        msg.put("payload", payload);
        return msg;
    }
}
