package io.github.kingqiang.cardgame.cardgamebackend.room.repository;

import io.github.kingqiang.cardgame.cardgamebackend.room.entity.RoomPlayer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * JPA 仓储：RoomPlayer 数据访问。
 */
public interface RoomPlayerRepository extends JpaRepository<RoomPlayer, Long> {

    List<RoomPlayer> findByRoomIdOrderBySeatAsc(String roomId);

    Optional<RoomPlayer> findByRoomIdAndUserId(String roomId, Long userId);

    Optional<RoomPlayer> findByRoomIdAndSeat(String roomId, Integer seat);

    long countByRoomId(String roomId);

    void deleteByRoomIdAndUserId(String roomId, Long userId);

    void deleteByRoomId(String roomId);

    List<RoomPlayer> findByUserId(Long userId);
}
