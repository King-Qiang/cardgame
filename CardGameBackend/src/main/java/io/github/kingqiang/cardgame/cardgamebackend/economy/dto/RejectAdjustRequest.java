package io.github.kingqiang.cardgame.cardgamebackend.economy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * API 请求体：RejectAdjust。
 */
@Getter
@Setter
public class RejectAdjustRequest {

    @NotBlank
    private String rejectReason;
}
