package io.github.kingqiang.cardgame.cardgamebackend.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API 数据传输对象：AdminRoleDto。
 */
@Getter
@Builder
public class AdminRoleDto {

    private final Long id;
    private final String name;
    private final String description;
    private final List<String> permissions;
    private final int permissionCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
