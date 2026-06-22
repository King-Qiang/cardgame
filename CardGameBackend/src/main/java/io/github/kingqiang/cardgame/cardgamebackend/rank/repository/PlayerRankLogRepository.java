package io.github.kingqiang.cardgame.cardgamebackend.rank.repository;

import io.github.kingqiang.cardgame.cardgamebackend.rank.entity.PlayerRankLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * JPA 仓储：PlayerRankLog 数据访问。
 */
public interface PlayerRankLogRepository extends JpaRepository<PlayerRankLog, Long> {

    List<PlayerRankLog> findTop5ByUserIdAndGameTypeOrderByCreatedAtDesc(Long userId, String gameType);
}
