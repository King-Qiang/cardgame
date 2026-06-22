package io.github.kingqiang.cardgame.cardgamebackend.economy.repository;

import io.github.kingqiang.cardgame.cardgamebackend.economy.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * JPA 仓储：WalletTransaction 数据访问。
 */
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long>, JpaSpecificationExecutor<WalletTransaction> {
}
