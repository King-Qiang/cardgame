package io.github.kingqiang.cardgame.cardgamebackend.room.websocket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kingqiang.cardgame.cardgamebackend.game.engine.GameAction;
import io.github.kingqiang.cardgame.cardgamebackend.room.service.GameSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;

/**
 * WebSocket 文本消息入口：BIND_ROOM、ACTION、心跳处理。
 */
@Component
@RequiredArgsConstructor
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final GameSessionService gameSessionService;
    private final GameMessageBroadcaster broadcaster;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long playerId = (Long) session.getAttributes().get("playerId");
        if (playerId != null) {
            sessionManager.register(playerId, session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long playerId = (Long) session.getAttributes().get("playerId");
        if (playerId == null) {
            return;
        }
        Map<String, Object> body = objectMapper.readValue(message.getPayload(), new TypeReference<>() {
        });
        String type = String.valueOf(body.get("type"));
        if ("PING".equals(type)) {
            broadcaster.sendPong(playerId);
            return;
        }
        if ("ACTION".equals(type)) {
            String roomId = String.valueOf(body.get("roomId"));
            sessionManager.bindRoom(playerId, roomId);
            int seq = ((Number) body.get("seq")).intValue();
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) body.get("payload");
            String action = String.valueOf(payload.get("action"));
            @SuppressWarnings("unchecked")
            List<String> cards = payload.get("cards") instanceof List<?> list
                    ? list.stream().map(String::valueOf).toList()
                    : null;
            GameAction gameAction = GameAction.builder()
                    .action(action)
                    .userId(playerId)
                    .cards(cards)
                    .build();
            try {
                gameSessionService.handleAction(roomId, playerId, seq, gameAction);
            } catch (Exception ex) {
                broadcaster.sendError(playerId, ex.getMessage());
            }
            return;
        }
        if ("RECONNECT".equals(type)) {
            String roomId = String.valueOf(body.get("roomId"));
            sessionManager.bindRoom(playerId, roomId);
            gameSessionService.reconnect(roomId, playerId);
            return;
        }
        if ("BIND_ROOM".equals(type)) {
            String roomId = String.valueOf(body.get("roomId"));
            sessionManager.bindRoom(playerId, roomId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long playerId = (Long) session.getAttributes().get("playerId");
        if (playerId != null) {
            sessionManager.unregister(playerId);
        }
    }
}
