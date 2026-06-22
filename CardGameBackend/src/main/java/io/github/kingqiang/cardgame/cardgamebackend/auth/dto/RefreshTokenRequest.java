package io.github.kingqiang.cardgame.cardgamebackend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * API 请求体：RefreshToken。
 */
@Getter
@Setter
public class RefreshTokenRequest {

    @NotBlank
    private String refreshToken;
}
