package io.github.kingqiang.cardgame.cardgamebackend.game.context;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存对局上下文仓库，key 为 roomId。
 */
@Component
public class GameContextStore {

    private final Map<String, GameContext> byRoom = new ConcurrentHashMap<>();
    private final Map<String, String> roomByRecord = new ConcurrentHashMap<>();

    public void save(GameContext ctx) {
        byRoom.put(ctx.getRoomId(), ctx);
        if (ctx.getRecordId() != null) {
            roomByRecord.put(ctx.getRecordId(), ctx.getRoomId());
        }
    }

    public GameContext getByRoom(String roomId) {
        return byRoom.get(roomId);
    }

    public GameContext getByRecord(String recordId) {
        String roomId = roomByRecord.get(recordId);
        return roomId != null ? byRoom.get(roomId) : null;
    }

    public void remove(String roomId) {
        GameContext ctx = byRoom.remove(roomId);
        if (ctx != null && ctx.getRecordId() != null) {
            roomByRecord.remove(ctx.getRecordId());
        }
    }
}
