package io.github.kingqiang.cardgame.cardgamebackend.economy.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * API 数据传输对象：ShopItemDto。
 */
@Getter
@Builder
public class ShopItemDto {

    private final Long id;
    private final String name;
    private final Long price;
    private final String currency;
    private final Object payload;
    private final Integer status;
    private final String statusLabel;
    private final Integer sortOrder;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
