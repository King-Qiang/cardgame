package io.github.kingqiang.cardgame.cardgamebackend.admin.service;

import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.AdminRecordDetailDto;
import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.RecordReplayMetaDto;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageRequest;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageResult;
import io.github.kingqiang.cardgame.cardgamebackend.common.exception.BusinessException;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import io.github.kingqiang.cardgame.cardgamebackend.record.dto.GameActionLogDto;
import io.github.kingqiang.cardgame.cardgamebackend.record.entity.GameActionLog;
import io.github.kingqiang.cardgame.cardgamebackend.record.entity.GameRecord;
import io.github.kingqiang.cardgame.cardgamebackend.record.repository.GameActionLogRepository;
import io.github.kingqiang.cardgame.cardgamebackend.record.repository.GameRecordRepository;
import io.github.kingqiang.cardgame.cardgamebackend.room.entity.GameRoom;
import io.github.kingqiang.cardgame.cardgamebackend.room.repository.GameRoomRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 业务服务：AdminRecord 领域逻辑。
 */
@Service
@RequiredArgsConstructor
public class AdminRecordService {

    private final GameRecordRepository gameRecordRepository;
    private final GameActionLogRepository gameActionLogRepository;
    private final GameRoomRepository gameRoomRepository;

    @Transactional(readOnly = true)
    public PageResult<AdminRecordListItemDto> list(String status, String gameType, String mode,
                                                   PageRequest pageRequest) {
        Specification<GameRecord> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (gameType != null && !gameType.isBlank()) {
                predicates.add(cb.equal(root.get("gameType"), gameType));
            }
            if (mode != null && !mode.isBlank()) {
                Subquery<String> roomSubquery = query.subquery(String.class);
                Root<GameRoom> roomRoot = roomSubquery.from(GameRoom.class);
                roomSubquery.select(roomRoot.get("roomId"))
                        .where(cb.equal(roomRoot.get("mode"), mode));
                predicates.add(root.get("roomId").in(roomSubquery));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<GameRecord> page = gameRecordRepository.findAll(spec,
                org.springframework.data.domain.PageRequest.of(pageRequest.getPage() - 1, pageRequest.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "startAt")));
        Set<String> roomIds = page.getContent().stream().map(GameRecord::getRoomId).collect(Collectors.toSet());
        Map<String, String> modeByRoom = gameRoomRepository.findAllById(roomIds).stream()
                .collect(Collectors.toMap(GameRoom::getRoomId, GameRoom::getMode));
        List<AdminRecordListItemDto> list = page.getContent().stream()
                .map(r -> AdminRecordListItemDto.builder()
                        .recordId(r.getRecordId())
                        .roomId(r.getRoomId())
                        .gameType(r.getGameType())
                        .mode(resolveMode(r, modeByRoom))
                        .status(r.getStatus())
                        .startAt(r.getStartAt())
                        .endAt(r.getEndAt())
                        .build())
                .toList();
        return PageResult.of(list, page.getTotalElements(), pageRequest.getPage(), pageRequest.getPageSize());
    }

    private String resolveMode(GameRecord record, Map<String, String> modeByRoom) {
        String roomMode = modeByRoom.get(record.getRoomId());
        if (roomMode != null && !roomMode.isBlank()) {
            return roomMode;
        }
        if (record.getResultJson() != null) {
            Object jsonMode = record.getResultJson().get("mode");
            if (jsonMode != null) {
                return String.valueOf(jsonMode);
            }
        }
        return "";
    }

    @Transactional(readOnly = true)
    public AdminRecordDetailDto detail(String recordId) {
        GameRecord record = findRecord(recordId);
        List<GameActionLogDto> actions = loadActionDtos(recordId);
        return toDetailDto(record, actions);
    }

    @Transactional(readOnly = true)
    public List<GameActionLogDto> replay(String recordId) {
        findRecord(recordId);
        return loadActionDtos(recordId);
    }

    @Transactional(readOnly = true)
    public RecordReplayMetaDto replayMeta(String recordId) {
        GameRecord record = findRecord(recordId);
        List<GameActionLogDto> actions = loadActionDtos(recordId);
        Map<Long, String> labels = new java.util.LinkedHashMap<>();
        for (GameActionLogDto action : actions) {
            labels.putIfAbsent(action.getUserId(), "玩家 " + action.getUserId());
        }
        return RecordReplayMetaDto.builder()
                .recordId(record.getRecordId())
                .gameType(record.getGameType())
                .totalSteps(actions.size())
                .playerLabels(labels)
                .actions(actions)
                .build();
    }

    private GameRecord findRecord(String recordId) {
        return gameRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR, "对局不存在"));
    }

    private List<GameActionLogDto> loadActionDtos(String recordId) {
        return gameActionLogRepository.findByRecordIdOrderBySeqAsc(recordId).stream()
                .map(this::toActionDto)
                .toList();
    }

    private AdminRecordDetailDto toDetailDto(GameRecord record, List<GameActionLogDto> actions) {
        return AdminRecordDetailDto.builder()
                .recordId(record.getRecordId())
                .roomId(record.getRoomId())
                .gameType(record.getGameType())
                .status(record.getStatus())
                .startAt(record.getStartAt())
                .endAt(record.getEndAt())
                .resultJson(record.getResultJson())
                .actions(actions)
                .build();
    }

    private GameActionLogDto toActionDto(GameActionLog log) {
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

    @Getter
    @Builder
    public static class AdminRecordListItemDto {
        private final String recordId;
        private final String roomId;
        private final String gameType;
        private final String mode;
        private final String status;
        private final LocalDateTime startAt;
        private final LocalDateTime endAt;
    }
}
