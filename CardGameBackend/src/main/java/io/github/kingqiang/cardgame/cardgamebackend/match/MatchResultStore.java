package io.github.kingqiang.cardgame.cardgamebackend.match;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 异步撮合成功后，为已从队列移除的玩家保留 roomId，供 GET /match/status 轮询。
 */
@Component
public class MatchResultStore {

    private static final long TTL_MS = 30 * 60 * 1000L;

    private final ConcurrentHashMap<Long, Entry> byUser = new ConcurrentHashMap<>();

    public void save(String gameType, String roomId, String matchMode, String matchTier, List<Long> playerIds) {
        long expiresAt = System.currentTimeMillis() + TTL_MS;
        Entry entry = new Entry(gameType, roomId, matchMode, matchTier, expiresAt);
        for (Long playerId : playerIds) {
            byUser.put(playerId, entry);
        }
    }

    public Optional<Entry> get(long userId, String gameType) {
        Entry entry = byUser.get(userId);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.expiresAt() < System.currentTimeMillis()) {
            byUser.remove(userId);
            return Optional.empty();
        }
        if (!entry.gameType().equals(gameType)) {
            return Optional.empty();
        }
        return Optional.of(entry);
    }

    public void remove(long userId) {
        byUser.remove(userId);
    }

    public record Entry(String gameType, String roomId, String matchMode, String matchTier, long expiresAt) {
    }
}
