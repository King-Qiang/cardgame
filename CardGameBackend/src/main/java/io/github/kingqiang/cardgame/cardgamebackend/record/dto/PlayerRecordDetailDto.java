package io.github.kingqiang.cardgame.cardgamebackend.record.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * API 数据传输对象：PlayerRecordDetailDto。
 */
@Getter
@Builder
public class PlayerRecordDetailDto {

    private final String recordId;
    private final String roomId;
    private final String gameType;
    private final String mode;
    private final String status;
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;
    private final long durationSec;
    private final Map<String, Object> result;
    private final MySettlementDto mySettlement;
    private final List<PlayerRecordParticipantDto> participants;
}
