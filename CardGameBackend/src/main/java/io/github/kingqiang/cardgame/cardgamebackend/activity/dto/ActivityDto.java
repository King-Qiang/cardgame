package io.github.kingqiang.cardgame.cardgamebackend.activity.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * API 数据传输对象：ActivityDto。
 */
@Getter
@Builder
public class ActivityDto {

    private final Long id;
    private final String code;
    private final String name;
    private final String type;
    private final Object configJson;
    private final Integer status;
    private final String statusLabel;
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
