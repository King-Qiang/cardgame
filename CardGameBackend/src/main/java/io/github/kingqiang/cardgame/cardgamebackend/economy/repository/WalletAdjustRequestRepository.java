package io.github.kingqiang.cardgame.cardgamebackend.economy.repository;

import io.github.kingqiang.cardgame.cardgamebackend.economy.entity.WalletAdjustRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * JPA 仓储：WalletAdjustRequest 数据访问。
 */
public interface WalletAdjustRequestRepository extends JpaRepository<WalletAdjustRequest, Long>, JpaSpecificationExecutor<WalletAdjustRequest> {
}
