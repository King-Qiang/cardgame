package io.github.kingqiang.cardgame.cardgamebackend.record.service;

import io.github.kingqiang.cardgame.cardgamebackend.record.dto.GameActionLogDto;
import io.github.kingqiang.cardgame.cardgamebackend.record.entity.GameActionLog;
import io.github.kingqiang.cardgame.cardgamebackend.record.repository.GameActionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 业务服务：GameActionLogQuery 领域逻辑。
 */
@Service
@RequiredArgsConstructor
public class GameActionLogQueryService {

    private final GameActionLogRepository gameActionLogRepository;

    @Transactional(readOnly = true)
    public List<GameActionLogDto> loadByRecordId(String recordId) {
        return gameActionLogRepository.findByRecordIdOrderBySeqAsc(recordId).stream()
                .map(this::toDto)
                .toList();
    }

    private GameActionLogDto toDto(GameActionLog log) {
        return GameActionLogDto.builder()
                .id(log.getId())
                .recordId(log.getRecordId())
                .seq(log.getSeq())
                .userId(log.getUserId())
                .action(log.getAction())
                .payload(log.getPayload())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
