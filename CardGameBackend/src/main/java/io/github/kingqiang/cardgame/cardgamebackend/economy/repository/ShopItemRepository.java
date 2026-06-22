package io.github.kingqiang.cardgame.cardgamebackend.economy.repository;

import io.github.kingqiang.cardgame.cardgamebackend.economy.entity.ShopItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * JPA 仓储：ShopItem 数据访问。
 */
public interface ShopItemRepository extends JpaRepository<ShopItem, Long>, JpaSpecificationExecutor<ShopItem> {

    List<ShopItem> findByStatusOrderBySortOrderAsc(Integer status);
}
