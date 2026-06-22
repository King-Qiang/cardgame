package io.github.kingqiang.cardgame.cardgamebackend.economy.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * API 数据传输对象：SystemConfigDto。
 */
@Getter
@Builder
public class SystemConfigDto {

    private final String configKey;
    private final Object configValue;
    private final String description;
    private final LocalDateTime updatedAt;
}
