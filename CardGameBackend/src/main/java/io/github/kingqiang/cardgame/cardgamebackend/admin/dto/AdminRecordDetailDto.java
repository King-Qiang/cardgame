package io.github.kingqiang.cardgame.cardgamebackend.admin.dto;

import io.github.kingqiang.cardgame.cardgamebackend.record.dto.GameActionLogDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * API 数据传输对象：AdminRecordDetailDto。
 */
@Getter
@Builder
public class AdminRecordDetailDto {

    private final String recordId;
    private final String roomId;
    private final String gameType;
    private final String status;
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;
    private final Map<String, Object> resultJson;
    private final List<GameActionLogDto> actions;
}
