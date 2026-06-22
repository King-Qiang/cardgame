package io.github.kingqiang.cardgame.cardgamebackend.match.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * API 响应体：QuickMatch。
 */
@Getter
@Builder
public class QuickMatchResponse {

    private final String status;
    private final String roomId;
    private final int queueSize;
    private final int requiredPlayers;
    private final String matchMode;
    private final String matchTier;
    private final Integer estimatedWaitSec;
}
