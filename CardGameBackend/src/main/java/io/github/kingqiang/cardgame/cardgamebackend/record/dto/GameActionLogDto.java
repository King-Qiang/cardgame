package io.github.kingqiang.cardgame.cardgamebackend.record.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * API 数据传输对象：GameActionLogDto。
 */
@Getter
@Builder
public class GameActionLogDto {

    private final Long id;
    private final String recordId;
    private final Integer seq;
    private final Long userId;
    private final String action;
    private final Map<String, Object> payload;
    private final LocalDateTime createdAt;
}
