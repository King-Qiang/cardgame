package io.github.kingqiang.cardgame.cardgamebackend.record.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageRequest;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageResult;
import io.github.kingqiang.cardgame.cardgamebackend.common.exception.BusinessException;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import io.github.kingqiang.cardgame.cardgamebackend.record.dto.GameActionLogDto;
import io.github.kingqiang.cardgame.cardgamebackend.record.dto.MySettlementDto;
import io.github.kingqiang.cardgame.cardgamebackend.record.dto.PlayerRecordDetailDto;
import io.github.kingqiang.cardgame.cardgamebackend.record.dto.PlayerRecordListItemDto;
import io.github.kingqiang.cardgame.cardgamebackend.record.dto.PlayerRecordParticipantDto;
import io.github.kingqiang.cardgame.cardgamebackend.record.entity.GameRecord;
import io.github.kingqiang.cardgame.cardgamebackend.record.entity.GameSettlement;
import io.github.kingqiang.cardgame.cardgamebackend.record.repository.GameRecordRepository;
import io.github.kingqiang.cardgame.cardgamebackend.record.repository.GameSettlementRepository;
import io.github.kingqiang.cardgame.cardgamebackend.room.entity.GameRoom;
import io.github.kingqiang.cardgame.cardgamebackend.room.repository.GameRoomRepository;
import io.github.kingqiang.cardgame.cardgamebackend.user.entity.Player;
import io.github.kingqiang.cardgame.cardgamebackend.user.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 业务服务：PlayerRecord 领域逻辑。
 */
@Service
@RequiredArgsConstructor
public class PlayerRecordService {

    private static final TypeReference<List<Map<String, Object>>> PARTICIPANTS_TYPE =
            new TypeReference<>() {};

    private final GameSettlementRepository gameSettlementRepository;
    private final GameRecordRepository gameRecordRepository;
    private final GameRoomRepository gameRoomRepository;
    private final PlayerRepository playerRepository;
    private final GameActionLogQueryService gameActionLogQueryService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public PageResult<PlayerRecordListItemDto> list(
            long userId, String gameType, String mode, PageRequest pageRequest) {
        int pageSize = Math.min(pageRequest.getPageSize(), 50);
        Page<Object[]> page = gameSettlementRepository.findPlayerFinishedRecords(
                userId,
                blankToNull(gameType),
                blankToNull(mode),
                org.springframework.data.domain.PageRequest.of(
                        pageRequest.getPage() - 1, pageSize));
        List<PlayerRecordListItemDto> list = page.getContent().stream()
                .map(this::toListItem)
                .toList();
        return PageResult.of(list, page.getTotalElements(), pageRequest.getPage(), pageSize);
    }

    @Transactional(readOnly = true)
    public PlayerRecordDetailDto detail(String recordId, long userId) {
        GameSettlement mySettlement = requireParticipation(recordId, userId);
        GameRecord record = gameRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR, "对局不存在"));
        Map<String, Object> resultJson = record.getResultJson() != null
                ? record.getResultJson()
                : Map.of();
        String mode = resolveMode(record, resultJson);
        List<PlayerRecordParticipantDto> participants = buildParticipants(recordId, resultJson);
        MySettlementDto myView = buildMySettlement(mySettlement, resultJson, participants, userId);
        Map<String, Object> result = extractResultSummary(resultJson);
        return PlayerRecordDetailDto.builder()
                .recordId(record.getRecordId())
                .roomId(record.getRoomId())
                .gameType(record.getGameType())
                .mode(mode)
                .status(record.getStatus())
                .startAt(record.getStartAt())
                .endAt(record.getEndAt())
                .durationSec(durationSec(record.getStartAt(), record.getEndAt()))
                .result(result)
                .mySettlement(myView)
                .participants(participants)
                .build();
    }

    @Transactional(readOnly = true)
    public List<GameActionLogDto> replay(String recordId, long userId) {
        requireParticipation(recordId, userId);
        return gameActionLogQueryService.loadByRecordId(recordId);
    }

    private GameSettlement requireParticipation(String recordId, long userId) {
        return gameSettlementRepository.findByRecordIdAndUserId(recordId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR, "对局不存在"));
    }

    private PlayerRecordListItemDto toListItem(Object[] row) {
        String recordId = (String) row[0];
        String roomId = (String) row[1];
        String gameType = (String) row[2];
        String status = (String) row[3];
        LocalDateTime startAt = toLocalDateTime(row[4]);
        LocalDateTime endAt = toLocalDateTime(row[5]);
        long goldDelta = row[6] != null ? ((Number) row[6]).longValue() : 0L;
        int score = row[7] != null ? ((Number) row[7]).intValue() : 0;
        Map<String, Object> resultJson = parseResultJson(row[8]);
        String roomMode = row[9] != null ? (String) row[9] : null;
        String mode = roomMode != null && !roomMode.isBlank()
                ? roomMode
                : stringValue(resultJson.get("mode"));
        Integer multiplier = intValue(resultJson.get("multiplier"));
        return PlayerRecordListItemDto.builder()
                .recordId(recordId)
                .roomId(roomId)
                .gameType(gameType)
                .mode(mode != null ? mode : "")
                .status(status)
                .startAt(startAt)
                .endAt(endAt)
                .durationSec(durationSec(startAt, endAt))
                .myGoldDelta(goldDelta)
                .myScore(score)
                .isWin(goldDelta > 0)
                .multiplier(multiplier)
                .build();
    }

    private List<PlayerRecordParticipantDto> buildParticipants(String recordId, Map<String, Object> resultJson) {
        List<Map<String, Object>> rawParticipants = parseParticipants(resultJson.get("participants"));
        if (!rawParticipants.isEmpty()) {
            Map<Long, String> nicknames = loadNicknames(rawParticipants.stream()
                    .map(p -> longValue(p.get("userId")))
                    .filter(Objects::nonNull)
                    .toList());
            Integer landlordSeat = intValue(resultJson.get("landlordSeat"));
            List<PlayerRecordParticipantDto> list = new ArrayList<>();
            for (Map<String, Object> p : rawParticipants) {
                Long uid = longValue(p.get("userId"));
                if (uid == null) {
                    continue;
                }
                Integer seat = intValue(p.get("seat"));
                long goldDelta = longValue(p.get("goldDelta")) != null ? longValue(p.get("goldDelta")) : 0L;
                list.add(PlayerRecordParticipantDto.builder()
                        .userId(uid)
                        .nickname(nicknames.getOrDefault(uid, "玩家" + uid))
                        .seat(seat)
                        .goldDelta(goldDelta)
                        .isLandlord(seat != null && seat.equals(landlordSeat))
                        .build());
            }
            return list;
        }
        List<GameSettlement> settlements = gameSettlementRepository.findByRecordId(recordId);
        Map<Long, String> nicknames = loadNicknames(settlements.stream()
                .map(GameSettlement::getUserId)
                .toList());
        return settlements.stream()
                .map(s -> PlayerRecordParticipantDto.builder()
                        .userId(s.getUserId())
                        .nickname(nicknames.getOrDefault(s.getUserId(), "玩家" + s.getUserId()))
                        .seat(null)
                        .goldDelta(s.getGoldDelta())
                        .isLandlord(false)
                        .build())
                .toList();
    }

    private MySettlementDto buildMySettlement(
            GameSettlement settlement,
            Map<String, Object> resultJson,
            List<PlayerRecordParticipantDto> participants,
            long userId) {
        Integer landlordSeat = intValue(resultJson.get("landlordSeat"));
        PlayerRecordParticipantDto mine = participants.stream()
                .filter(p -> userId == p.getUserId())
                .findFirst()
                .orElse(null);
        Integer seat = mine != null ? mine.getSeat() : null;
        boolean isLandlord = seat != null && seat.equals(landlordSeat);
        return MySettlementDto.builder()
                .seat(seat)
                .goldDelta(settlement.getGoldDelta())
                .score(settlement.getScore())
                .isLandlord(isLandlord)
                .isWin(settlement.getGoldDelta() > 0)
                .build();
    }

    private Map<String, Object> extractResultSummary(Map<String, Object> resultJson) {
        Map<String, Object> summary = new LinkedHashMap<>();
        if (resultJson.containsKey("winnerSeat")) {
            summary.put("winnerSeat", resultJson.get("winnerSeat"));
        }
        if (resultJson.containsKey("landlordSeat")) {
            summary.put("landlordSeat", resultJson.get("landlordSeat"));
        }
        if (resultJson.containsKey("multiplier")) {
            summary.put("multiplier", resultJson.get("multiplier"));
        }
        return summary;
    }

    private String resolveMode(GameRecord record, Map<String, Object> resultJson) {
        String fromJson = stringValue(resultJson.get("mode"));
        if (fromJson != null && !fromJson.isBlank()) {
            return fromJson;
        }
        return gameRoomRepository.findById(record.getRoomId())
                .map(GameRoom::getMode)
                .orElse("");
    }

    private Map<Long, String> loadNicknames(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return playerRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(Player::getId, Player::getNickname));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseResultJson(Object raw) {
        if (raw == null) {
            return Map.of();
        }
        if (raw instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        if (raw instanceof String json) {
            try {
                return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            } catch (Exception ignored) {
                return Map.of();
            }
        }
        try {
            return objectMapper.convertValue(raw, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ignored) {
            return Map.of();
        }
    }

    private List<Map<String, Object>> parseParticipants(Object raw) {
        if (raw == null) {
            return List.of();
        }
        if (raw instanceof List<?> list) {
            List<Map<String, Object>> out = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    Map<String, Object> converted = new HashMap<>();
                    map.forEach((k, v) -> converted.put(String.valueOf(k), v));
                    out.add(converted);
                }
            }
            return out;
        }
        try {
            return objectMapper.convertValue(raw, PARTICIPANTS_TYPE);
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private long durationSec(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null) {
            return 0L;
        }
        return Math.max(0, Duration.between(startAt, endAt).getSeconds());
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime ldt) {
            return ldt;
        }
        if (value instanceof Timestamp ts) {
            return ts.toLocalDateTime();
        }
        return null;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String stringValue(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    private Integer intValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Long longValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
