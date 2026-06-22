package io.github.kingqiang.cardgame.cardgamebackend.economy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * API 请求体：CreateRechargeOrder。
 */
@Getter
@Setter
public class CreateRechargeOrderRequest {

    @NotNull
    @Min(1)
    private Long amount;

    @NotNull
    @Min(1)
    private Long goldAmount;

    @NotBlank
    private String payChannel = "WECHAT";
}
