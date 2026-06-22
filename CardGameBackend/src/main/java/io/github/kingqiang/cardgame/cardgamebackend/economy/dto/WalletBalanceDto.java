package io.github.kingqiang.cardgame.cardgamebackend.economy.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * API 数据传输对象：WalletBalanceDto。
 */
@Getter
@Builder
public class WalletBalanceDto {

    private final Long userId;
    private final Long gold;
}
