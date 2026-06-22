package io.github.kingqiang.cardgame.cardgamebackend.rank.enums;

import java.util.Arrays;
import java.util.List;

/**
 * 枚举：RankTier。
 */
public enum RankTier {

    BRONZE, SILVER, GOLD, PLATINUM, DIAMOND;

    private static final List<RankTier> ORDER = List.of(values());

    public static RankTier fromString(String value) {
        if (value == null || value.isBlank()) {
            return BRONZE;
        }
        return Arrays.stream(values())
                .filter(t -> t.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(BRONZE);
    }

    public List<RankTier> expandTiers(int expandLevel) {
        int index = ORDER.indexOf(this);
        if (index < 0) {
            return List.of(BRONZE);
        }
        int low = Math.max(0, index - expandLevel);
        int high = Math.min(ORDER.size() - 1, index + expandLevel);
        return ORDER.subList(low, high + 1);
    }
}
