package io.github.kingqiang.cardgame.cardgamebackend.economy.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * WalletChangeResult。
 */
@Getter
@Builder
public class WalletChangeResult {

    private final Long transactionId;
    private final Long userId;
    private final long amount;
    private final long balanceAfter;
}
