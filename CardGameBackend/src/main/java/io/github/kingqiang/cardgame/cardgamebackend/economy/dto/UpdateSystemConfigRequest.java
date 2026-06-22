package io.github.kingqiang.cardgame.cardgamebackend.economy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * API 请求体：UpdateSystemConfig。
 */
@Getter
@Setter
public class UpdateSystemConfigRequest {

    @NotNull
    private Object configValue;
}
