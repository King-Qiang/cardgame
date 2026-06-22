package io.github.kingqiang.cardgame.cardgamebackend.rank;

import io.github.kingqiang.cardgame.cardgamebackend.rank.enums.RankTier;

import java.util.ArrayList;
import java.util.List;

/**
 * 辅助工具：RankMatch。
 */
public final class RankMatchHelper {

    private static final long EXPAND_WAIT_MS = 30_000L;
    private static final long EXPAND_STEP_MS = 15_000L;

    private RankMatchHelper() {
    }

    public static List<String> queueKeysForMatch(String gameType, String tier, long waitMs) {
        int expandLevel = expandLevel(waitMs);
        RankTier rankTier = RankTier.fromString(tier);
        List<String> keys = new ArrayList<>();
        for (RankTier t : rankTier.expandTiers(expandLevel)) {
            keys.add(queueKey(gameType, t.name()));
        }
        return keys;
    }

    public static String queueKey(String gameType, String tier) {
        return gameType + ":" + tier;
    }

    public static int expandLevel(long waitMs) {
        if (waitMs < EXPAND_WAIT_MS) {
            return 0;
        }
        return (int) Math.min(2, (waitMs - EXPAND_WAIT_MS) / EXPAND_STEP_MS + 1);
    }

    public static int estimatedWaitSec(long waitMs) {
        if (waitMs < EXPAND_WAIT_MS) {
            return Math.max(0, (int) ((EXPAND_WAIT_MS - waitMs) / 1000));
        }
        return 0;
    }
}
