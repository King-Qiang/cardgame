package io.github.kingqiang.cardgame.cardgamebackend.record.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * API 数据传输对象：MySettlementDto。
 */
@Getter
@Builder
public class MySettlementDto {

    private final Integer seat;
    private final long goldDelta;
    private final int score;
    private final boolean isLandlord;
    private final boolean isWin;
}
