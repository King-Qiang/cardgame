package io.github.kingqiang.cardgame.cardgamebackend.economy.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * API 数据传输对象：WalletTransactionDto。
 */
@Getter
@Builder
public class WalletTransactionDto {

    private final Long id;
    private final Long userId;
    private final String type;
    private final Long amount;
    private final Long balanceAfter;
    private final String refType;
    private final String refId;
    private final String remark;
    private final LocalDateTime createdAt;
}
