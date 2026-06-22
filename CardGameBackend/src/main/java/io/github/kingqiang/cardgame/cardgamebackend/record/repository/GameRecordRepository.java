package io.github.kingqiang.cardgame.cardgamebackend.record.repository;

import io.github.kingqiang.cardgame.cardgamebackend.record.entity.GameRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * JPA 仓储：GameRecord 数据访问。
 */
public interface GameRecordRepository extends JpaRepository<GameRecord, String>, JpaSpecificationExecutor<GameRecord> {
}
