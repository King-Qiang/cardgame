package io.github.kingqiang.cardgame.cardgamebackend.bot;

import io.github.kingqiang.cardgame.cardgamebackend.economy.entity.SystemConfig;
import io.github.kingqiang.cardgame.cardgamebackend.economy.repository.SystemConfigRepository;
import io.github.kingqiang.cardgame.cardgamebackend.room.entity.GameRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 业务服务：BotConfig 领域逻辑。
 */
@Service
@RequiredArgsConstructor
public class BotConfigService {

    private static final String PVE_KEY = "game.pve";
    private static final String BOT_KEY = "game.bot";

    private final SystemConfigRepository systemConfigRepository;

    public boolean isPveEnabled() {
        Map<String, Object> config = readJson(PVE_KEY);
        Object enabled = config.get("enabled");
        return enabled == null || Boolean.parseBoolean(String.valueOf(enabled));
    }

    public BotDifficulty defaultDifficulty() {
        Map<String, Object> config = readJson(PVE_KEY);
        return BotDifficulty.fromString(stringValue(config.get("defaultDifficulty")));
    }

    public BotDifficulty resolveDifficulty(GameRoom room) {
        if (room != null && room.getConfigJson() != null) {
            Object value = room.getConfigJson().get("botDifficulty");
            if (value != null) {
                return BotDifficulty.fromString(String.valueOf(value));
            }
        }
        return defaultDifficulty();
    }

    public long resolveActionDelayMs(GameRoom room) {
        if (room != null && room.getConfigJson() != null) {
            Object value = room.getConfigJson().get("botActionDelayMs");
            if (value instanceof Number number) {
                return Math.max(0, number.longValue());
            }
        }
        Map<String, Object> config = readJson(BOT_KEY);
        long min = longValue(config.get("minDelayMs"), 500L);
        long max = longValue(config.get("maxDelayMs"), 1500L);
        if (max < min) {
            max = min;
        }
        if (min == max) {
            return min;
        }
        return ThreadLocalRandom.current().nextLong(min, max + 1);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readJson(String key) {
        return systemConfigRepository.findById(key)
                .map(SystemConfig::getConfigValue)
                .filter(Map.class::isInstance)
                .map(value -> (Map<String, Object>) value)
                .orElse(Map.of());
    }

    private static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static long longValue(Object value, long defaultValue) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return defaultValue;
    }

    public List<String> enabledDifficulties() {
        Map<String, Object> config = readJson(BOT_KEY);
        Object difficulties = config.get("difficulties");
        if (difficulties instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of(BotDifficulty.EASY.name());
    }
}
