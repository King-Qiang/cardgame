package io.github.kingqiang.cardgame.cardgamebackend.economy.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * API 数据传输对象：RechargeOrderDto。
 */
@Getter
@Builder
public class RechargeOrderDto {

    private final String orderNo;
    private final Long userId;
    private final Long amount;
    private final Long goldAmount;
    private final String payChannel;
    private final String status;
    private final LocalDateTime paidAt;
    private final LocalDateTime createdAt;
}
