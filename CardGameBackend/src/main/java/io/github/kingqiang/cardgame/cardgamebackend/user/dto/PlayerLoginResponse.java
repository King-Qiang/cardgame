package io.github.kingqiang.cardgame.cardgamebackend.user.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * API 响应体：PlayerLogin。
 */
@Getter
@Builder
public class PlayerLoginResponse {

    private final String accessToken;
    private final String refreshToken;
    private final long expiresIn;
    private final PlayerUserDto user;

    @Getter
    @Builder
    public static class PlayerUserDto {
        private final Long id;
        private final String nickname;
        private final String avatar;
    }
}
