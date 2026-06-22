package io.github.kingqiang.cardgame.cardgamebackend.economy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * API 请求体：ShopBuy。
 */
@Getter
@Setter
public class ShopBuyRequest {

    @NotNull
    private Long itemId;

    @Min(1)
    private int quantity = 1;
}
