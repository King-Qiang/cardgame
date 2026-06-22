package io.github.kingqiang.cardgame.cardgamebackend.room.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 会话/资源管理：WebSocketSession。
 */
@Component
public class WebSocketSessionManager {

    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<Long, String> userRoom = new ConcurrentHashMap<>();

    public void register(long userId, WebSocketSession session) {
        sessions.put(userId, session);
    }

    public void unregister(long userId) {
        sessions.remove(userId);
        userRoom.remove(userId);
    }

    public WebSocketSession getSession(long userId) {
        return sessions.get(userId);
    }

    public void bindRoom(long userId, String roomId) {
        userRoom.put(userId, roomId);
    }

    public String getRoom(long userId) {
        return userRoom.get(userId);
    }

    public Set<Long> userIdsInRoom(String roomId) {
        return userRoom.entrySet().stream()
                .filter(e -> roomId.equals(e.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public int onlinePlayerCount() {
        return sessions.size();
    }
}
