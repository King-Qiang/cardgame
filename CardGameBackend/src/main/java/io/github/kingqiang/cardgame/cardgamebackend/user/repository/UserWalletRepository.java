package io.github.kingqiang.cardgame.cardgamebackend.user.repository;

import io.github.kingqiang.cardgame.cardgamebackend.user.entity.UserWallet;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA 仓储：UserWallet 数据访问。
 */
public interface UserWalletRepository extends JpaRepository<UserWallet, Long> {
}
