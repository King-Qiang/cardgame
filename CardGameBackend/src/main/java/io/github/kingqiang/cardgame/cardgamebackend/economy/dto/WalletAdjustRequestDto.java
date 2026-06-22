package io.github.kingqiang.cardgame.cardgamebackend.economy.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * API 数据传输对象：WalletAdjustRequestDto。
 */
@Getter
@Builder
public class WalletAdjustRequestDto {

    private final Long id;
    private final Long userId;
    private final String adjustType;
    private final Long amount;
    private final String reason;
    private final String status;
    private final Long applicantId;
    private final Long approverId;
    private final LocalDateTime approvedAt;
    private final String rejectReason;
    private final LocalDateTime createdAt;
}
