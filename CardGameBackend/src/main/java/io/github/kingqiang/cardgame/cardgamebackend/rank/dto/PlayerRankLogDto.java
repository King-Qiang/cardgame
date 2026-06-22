package io.github.kingqiang.cardgame.cardgamebackend.rank.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * API 数据传输对象：PlayerRankLogDto。
 */
@Getter
@Builder
public class PlayerRankLogDto {

    private final Long id;
    private final String recordId;
    private final int deltaPoints;
    private final String tierBefore;
    private final String tierAfter;
    private final int pointsBefore;
    private final int pointsAfter;
    private final LocalDateTime createdAt;
}
