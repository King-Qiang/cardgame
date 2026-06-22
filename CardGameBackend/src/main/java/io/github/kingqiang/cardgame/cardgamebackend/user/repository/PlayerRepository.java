package io.github.kingqiang.cardgame.cardgamebackend.user.repository;

import io.github.kingqiang.cardgame.cardgamebackend.user.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * JPA 仓储：Player 数据访问。
 */
public interface PlayerRepository extends JpaRepository<Player, Long>, JpaSpecificationExecutor<Player> {

    Optional<Player> findByOpenid(String openid);
}
