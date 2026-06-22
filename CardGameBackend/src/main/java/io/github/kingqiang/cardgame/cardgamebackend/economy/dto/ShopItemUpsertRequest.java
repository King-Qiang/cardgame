package io.github.kingqiang.cardgame.cardgamebackend.economy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

/**
 * API 请求体：ShopItemUpsert。
 */
@Getter
@Setter
public class ShopItemUpsertRequest {

    @NotBlank
    private String name;

    @NotNull
    @PositiveOrZero
    private Long price;

    @NotBlank
    private String currency;

    private Object payload;

    @NotNull
    private Integer status;

    @NotNull
    private Integer sortOrder;
}
