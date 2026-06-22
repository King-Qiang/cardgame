package io.github.kingqiang.cardgame.cardgamebackend.activity.repository;

import io.github.kingqiang.cardgame.cardgamebackend.activity.entity.PlayerSignRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * JPA 仓储：PlayerSignRecord 数据访问。
 */
public interface PlayerSignRecordRepository extends JpaRepository<PlayerSignRecord, Long> {

    Optional<PlayerSignRecord> findByUserIdAndSignDate(Long userId, LocalDate signDate);

    Optional<PlayerSignRecord> findTopByUserIdOrderBySignDateDesc(Long userId);
}
