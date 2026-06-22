package io.github.kingqiang.cardgame.cardgamebackend.admin.service;

import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.OperationActions;
import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.service.OperationLogService;
import io.github.kingqiang.cardgame.cardgamebackend.auth.security.SecurityUtils;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.RequestUtils;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageRequest;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageResult;
import io.github.kingqiang.cardgame.cardgamebackend.common.exception.BusinessException;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import io.github.kingqiang.cardgame.cardgamebackend.record.entity.GameRecord;
import io.github.kingqiang.cardgame.cardgamebackend.record.repository.GameRecordRepository;
import io.github.kingqiang.cardgame.cardgamebackend.room.dto.RoomDetailResponse;
import io.github.kingqiang.cardgame.cardgamebackend.room.entity.GameRoom;
import io.github.kingqiang.cardgame.cardgamebackend.room.repository.GameRoomRepository;
import io.github.kingqiang.cardgame.cardgamebackend.room.service.RoomService;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 业务服务：AdminRoom 领域逻辑。
 */
@Service
@RequiredArgsConstructor
public class AdminRoomService {

    private final GameRoomRepository gameRoomRepository;
    private final RoomService roomService;
    private final OperationLogService operationLogService;

    @Transactional(readOnly = true)
    public PageResult<AdminRoomListItemDto> list(String status, String gameType, String mode,
                                                 PageRequest pageRequest) {
        Specification<GameRoom> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (gameType != null && !gameType.isBlank()) {
                predicates.add(cb.equal(root.get("gameType"), gameType));
            }
            if (mode != null && !mode.isBlank()) {
                predicates.add(cb.equal(root.get("mode"), mode));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<GameRoom> page = gameRoomRepository.findAll(spec,
                org.springframework.data.domain.PageRequest.of(pageRequest.getPage() - 1, pageRequest.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")));
        List<AdminRoomListItemDto> list = page.getContent().stream()
                .map(r -> AdminRoomListItemDto.builder()
                        .roomId(r.getRoomId())
                        .gameType(r.getGameType())
                        .mode(r.getMode())
                        .status(r.getStatus())
                        .ownerId(r.getOwnerId())
                        .playerCount(roomService.toDetail(r).getPlayers().size())
                        .createdAt(r.getCreatedAt())
                        .build())
                .toList();
        return PageResult.of(list, page.getTotalElements(), pageRequest.getPage(), pageRequest.getPageSize());
    }

    @Transactional(readOnly = true)
    public RoomDetailResponse detail(String roomId) {
        return roomService.getRoom(roomId);
    }

    @Transactional
    public void disband(String roomId, String reason) {
        GameRoom room = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        room.setStatus("DISBANDED");
        room.setUpdatedAt(LocalDateTime.now());
        gameRoomRepository.save(room);

        operationLogService.log(
                SecurityUtils.requireAdmin().getId(),
                OperationActions.ROOM_DISBAND,
                "ROOM",
                roomId,
                Map.of("reason", reason),
                RequestUtils.clientIp()
        );
    }

    @Transactional
    public void kick(String roomId, long userId, String reason) {
        roomService.adminKick(roomId, userId, reason);
        operationLogService.log(
                SecurityUtils.requireAdmin().getId(),
                OperationActions.ROOM_KICK,
                "ROOM",
                roomId,
                Map.of("userId", userId, "reason", reason != null ? reason : ""),
                RequestUtils.clientIp()
        );
    }

    @Getter
    @Builder
    public static class AdminRoomListItemDto {
        private final String roomId;
        private final String gameType;
        private final String mode;
        private final String status;
        private final Long ownerId;
        private final int playerCount;
        private final LocalDateTime createdAt;
    }
}
