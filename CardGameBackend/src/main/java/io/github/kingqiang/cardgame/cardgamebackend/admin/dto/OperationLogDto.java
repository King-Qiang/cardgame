package io.github.kingqiang.cardgame.cardgamebackend.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * API 数据传输对象：OperationLogDto。
 */
@Getter
@Builder
public class OperationLogDto {

    private final Long id;
    private final Long operatorId;
    private final String operatorName;
    private final String action;
    private final String targetType;
    private final String targetId;
    private final Map<String, Object> detail;
    private final String ip;
    private final LocalDateTime createdAt;
}
