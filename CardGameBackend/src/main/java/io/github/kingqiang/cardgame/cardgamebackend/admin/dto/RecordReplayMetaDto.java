package io.github.kingqiang.cardgame.cardgamebackend.admin.dto;

import io.github.kingqiang.cardgame.cardgamebackend.record.dto.GameActionLogDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * API 数据传输对象：RecordReplayMetaDto。
 */
@Getter
@Builder
public class RecordReplayMetaDto {

    private final String recordId;
    private final String gameType;
    private final int totalSteps;
    private final Map<Long, String> playerLabels;
    private final List<GameActionLogDto> actions;
}
