package io.github.kingqiang.cardgame.cardgamebackend.economy.repository;

import io.github.kingqiang.cardgame.cardgamebackend.economy.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA 仓储：SystemConfig 数据访问。
 */
public interface SystemConfigRepository extends JpaRepository<SystemConfig, String> {
}
