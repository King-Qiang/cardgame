package io.github.kingqiang.cardgame.cardgamebackend.user.repository;

import io.github.kingqiang.cardgame.cardgamebackend.user.entity.UserBan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * JPA 仓储：UserBan 数据访问。
 */
public interface UserBanRepository extends JpaRepository<UserBan, Long> {

    @Query("""
            select b from UserBan b
            where b.userId = :userId and b.revokedAt is null
            and (b.banUntil is null or b.banUntil > :now)
            order by b.createdAt desc
            """)
    Optional<UserBan> findActiveBan(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
