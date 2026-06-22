package io.github.kingqiang.cardgame.cardgamebackend.rank.repository;

import io.github.kingqiang.cardgame.cardgamebackend.rank.entity.PlayerRank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * JPA 仓储：PlayerRank 数据访问。
 */
public interface PlayerRankRepository extends JpaRepository<PlayerRank, PlayerRank.PlayerRankId> {

    Optional<PlayerRank> findByUserIdAndGameTypeAndSeasonId(Long userId, String gameType, String seasonId);

    List<PlayerRank> findByUserIdInAndGameTypeAndSeasonId(Collection<Long> userIds, String gameType, String seasonId);

    Page<PlayerRank> findByGameTypeAndSeasonIdOrderByPointsDesc(String gameType, String seasonId, Pageable pageable);

    Page<PlayerRank> findByGameTypeAndSeasonIdAndTierOrderByPointsDesc(
            String gameType, String seasonId, String tier, Pageable pageable);
}
