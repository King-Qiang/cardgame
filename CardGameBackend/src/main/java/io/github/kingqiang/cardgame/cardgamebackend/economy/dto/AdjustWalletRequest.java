package io.github.kingqiang.cardgame.cardgamebackend.economy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * API 请求体：AdjustWallet。
 */
@Getter
@Setter
public class AdjustWalletRequest {

    @NotBlank
    private String adjustType;

    @NotNull
    @Positive
    private Long amount;

    @NotBlank
    private String reason;
}
