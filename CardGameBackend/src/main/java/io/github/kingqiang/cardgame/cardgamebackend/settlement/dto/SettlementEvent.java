package io.github.kingqiang.cardgame.cardgamebackend.settlement.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SettlementEvent。
 */
@Getter
@Builder
public class SettlementEvent {

    private final String recordId;
    private final String roomId;
    private final String gameType;
    private final String mode;
    private final Integer winnerSeat;
    private final List<SettlementLine> settlements;
    private final LocalDateTime createdAt;

    @Getter
    @Builder
    public static class SettlementLine {
        private final long userId;
        private final int seat;
        private final long goldDelta;
        private final long scoreDelta;
    }
}
