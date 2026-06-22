package io.github.kingqiang.cardgame.cardgamebackend.auth.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * API 响应体：TokenPair。
 */
@Getter
@Builder
public class TokenPairResponse {

    private final String accessToken;
    private final String refreshToken;
    private final long expiresIn;
}
