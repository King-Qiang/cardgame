package io.github.kingqiang.cardgame.cardgamebackend.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * API 响应体：AdminLogin。
 */
@Getter
@Builder
public class AdminLoginResponse {

    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private AdminUserDto user;
    private List<String> permissions;

    @Getter
    @Builder
    public static class AdminUserDto {
        private Long id;
        private String username;
        private String realName;
    }
}
