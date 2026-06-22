package io.github.kingqiang.cardgame.cardgamebackend.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * API 数据传输对象：AdminAccountDto。
 */
@Getter
@Builder
public class AdminAccountDto {

    private final Long id;
    private final String username;
    private final String realName;
    private final Long roleId;
    private final String roleName;
    private final Integer status;
    private final String statusLabel;
    private final LocalDateTime lastLoginAt;
    private final String lastLoginIp;
    private final LocalDateTime createdAt;
}
