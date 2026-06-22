package io.github.kingqiang.cardgame.cardgamebackend.economy.repository;

import io.github.kingqiang.cardgame.cardgamebackend.economy.entity.RechargeOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * JPA 仓储：RechargeOrder 数据访问。
 */
public interface RechargeOrderRepository extends JpaRepository<RechargeOrder, String>, JpaSpecificationExecutor<RechargeOrder> {
}
