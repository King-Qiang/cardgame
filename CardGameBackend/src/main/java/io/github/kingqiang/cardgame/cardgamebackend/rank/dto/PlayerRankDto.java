package io.github.kingqiang.cardgame.cardgamebackend.rank.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * API 数据传输对象：PlayerRankDto。
 */
@Getter
@Builder
public class PlayerRankDto {

    private final Long userId;
    private final String gameType;
    private final String seasonId;
    private final String tier;
    private final int points;
    private final int wins;
    private final int losses;
    private final String nextTier;
    private final Integer pointsToNextTier;
}
