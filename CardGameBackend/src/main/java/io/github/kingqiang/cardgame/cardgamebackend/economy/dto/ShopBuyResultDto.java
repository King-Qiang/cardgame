package io.github.kingqiang.cardgame.cardgamebackend.economy.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * API 数据传输对象：ShopBuyResultDto。
 */
@Getter
@Builder
public class ShopBuyResultDto {

    private final Long itemId;
    private final int quantity;
    private final Long totalCost;
    private final Long balanceAfter;
    private final Long grantedGold;
}
