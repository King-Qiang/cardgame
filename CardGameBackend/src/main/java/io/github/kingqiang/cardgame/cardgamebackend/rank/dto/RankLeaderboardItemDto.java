package io.github.kingqiang.cardgame.cardgamebackend.rank.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * API 数据传输对象：RankLeaderboardItemDto。
 */
@Getter
@Builder
public class RankLeaderboardItemDto {

    private final int rank;
    private final Long userId;
    private final String nickname;
    private final String tier;
    private final int points;
    private final int wins;
    private final int losses;
}
