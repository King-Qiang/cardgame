package io.github.kingqiang.cardgame.cardgamebackend.activity.repository;

import io.github.kingqiang.cardgame.cardgamebackend.activity.entity.ActivityConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * JPA 仓储：ActivityConfig 数据访问。
 */
public interface ActivityConfigRepository extends JpaRepository<ActivityConfig, Long>, JpaSpecificationExecutor<ActivityConfig> {

    Optional<ActivityConfig> findByCode(String code);

    boolean existsByCode(String code);
}
