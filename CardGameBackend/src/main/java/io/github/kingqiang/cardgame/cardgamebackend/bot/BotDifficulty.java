package io.github.kingqiang.cardgame.cardgamebackend.bot;

/**
 * 枚举：BotDifficulty。
 */
public enum BotDifficulty {
    EASY,
    NORMAL,
    HARD;

    public static BotDifficulty fromString(String value) {
        if (value == null || value.isBlank()) {
            return EASY;
        }
        try {
            return BotDifficulty.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return EASY;
        }
    }
}
