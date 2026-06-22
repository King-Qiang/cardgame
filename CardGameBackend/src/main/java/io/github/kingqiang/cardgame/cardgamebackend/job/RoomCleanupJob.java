package io.github.kingqiang.cardgame.cardgamebackend.job;

import io.github.kingqiang.cardgame.cardgamebackend.common.config.CardgameProperties;
import io.github.kingqiang.cardgame.cardgamebackend.game.context.GameContextStore;
import io.github.kingqiang.cardgame.cardgamebackend.room.entity.GameRoom;
import io.github.kingqiang.cardgame.cardgamebackend.room.repository.GameRoomRepository;
import io.github.kingqiang.cardgame.cardgamebackend.room.repository.RoomPlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务：RoomCleanup。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoomCleanupJob {

    private final GameRoomRepository gameRoomRepository;
    private final RoomPlayerRepository roomPlayerRepository;
    private final GameContextStore gameContextStore;
    private final CardgameProperties cardgameProperties;

    @Scheduled(fixedDelayString = "${cardgame.job.cleanup-interval-ms:300000}")
    @Transactional
    public void cleanupStaleWaitingRooms() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(cardgameProperties.job().staleRoomHours());
        List<GameRoom> staleRooms = gameRoomRepository.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("status"), "WAITING"),
                cb.lessThan(root.get("createdAt"), threshold)
        ));
        if (staleRooms.isEmpty()) {
            return;
        }
        for (GameRoom room : staleRooms) {
            room.setStatus("DISBANDED");
            room.setUpdatedAt(LocalDateTime.now());
            roomPlayerRepository.deleteByRoomId(room.getRoomId());
            gameContextStore.remove(room.getRoomId());
        }
        gameRoomRepository.saveAll(staleRooms);
        log.info("Cleaned {} stale waiting rooms older than {} hours", staleRooms.size(),
                cardgameProperties.job().staleRoomHours());
    }
}
