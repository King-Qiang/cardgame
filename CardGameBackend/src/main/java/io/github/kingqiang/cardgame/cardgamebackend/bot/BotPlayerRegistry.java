package io.github.kingqiang.cardgame.cardgamebackend.bot;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bot 玩家 ID 池（900001–900003），同局避免重复 userId。
 */
@Component
public class BotPlayerRegistry {

    public static final long BOT_ID_MIN = 900_001L;
    public static final long BOT_ID_MAX = 900_003L;

    private static final List<Long> SYSTEM_BOT_IDS = List.of(900_001L, 900_002L, 900_003L);

    private final Set<Long> botIds = ConcurrentHashMap.newKeySet();

    public BotPlayerRegistry() {
        botIds.addAll(SYSTEM_BOT_IDS);
    }

    public boolean isBot(long userId) {
        return userId >= BOT_ID_MIN && userId <= BOT_ID_MAX;
    }

    public List<Long> systemBotIds() {
        return SYSTEM_BOT_IDS;
    }

    public List<Long> pickBots(int count, Set<Long> excludedUserIds) {
        return SYSTEM_BOT_IDS.stream()
                .filter(id -> excludedUserIds == null || !excludedUserIds.contains(id))
                .limit(count)
                .toList();
    }
}
