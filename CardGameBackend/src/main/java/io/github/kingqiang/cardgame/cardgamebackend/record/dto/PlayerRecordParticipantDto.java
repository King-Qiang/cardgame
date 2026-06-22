package io.github.kingqiang.cardgame.cardgamebackend.record.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * API 数据传输对象：PlayerRecordParticipantDto。
 */
@Getter
@Builder
public class PlayerRecordParticipantDto {

    private final Long userId;
    private final String nickname;
    private final Integer seat;
    private final long goldDelta;
    private final boolean isLandlord;
}
