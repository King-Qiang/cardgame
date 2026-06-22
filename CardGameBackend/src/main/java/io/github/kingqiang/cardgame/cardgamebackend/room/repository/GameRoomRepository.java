package io.github.kingqiang.cardgame.cardgamebackend.room.repository;

import io.github.kingqiang.cardgame.cardgamebackend.room.entity.GameRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * JPA 仓储：GameRoom 数据访问。
 */
public interface GameRoomRepository extends JpaRepository<GameRoom, String>, JpaSpecificationExecutor<GameRoom> {
}
