package io.github.kingqiang.cardgame.cardgamebackend.record.repository;

import io.github.kingqiang.cardgame.cardgamebackend.record.entity.GameActionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * JPA 仓储：GameActionLog 数据访问。
 */
public interface GameActionLogRepository extends JpaRepository<GameActionLog, Long> {

    List<GameActionLog> findByRecordIdOrderBySeqAsc(String recordId);
}
