package io.github.kingqiang.cardgame.cardgamebackend.record.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * API 数据传输对象：PlayerRecordListItemDto。
 */
@Getter
@Builder
public class PlayerRecordListItemDto {

    private final String recordId;
    private final String roomId;
    private final String gameType;
    private final String mode;
    private final String status;
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;
    private final long durationSec;
    private final long myGoldDelta;
    private final int myScore;
    private final boolean isWin;
    private final Integer multiplier;
}
