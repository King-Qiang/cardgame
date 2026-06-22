package io.github.kingqiang.cardgame.cardgamebackend.user.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * API 数据传输对象：RankSummaryDto。
 */
@Getter
@Builder
public class RankSummaryDto {

    private final String gameType;
    private final String tier;
    private final int points;
    private final int wins;
    private final int losses;
}
