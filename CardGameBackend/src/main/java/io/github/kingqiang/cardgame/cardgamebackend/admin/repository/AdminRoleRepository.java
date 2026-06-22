package io.github.kingqiang.cardgame.cardgamebackend.admin.repository;

import io.github.kingqiang.cardgame.cardgamebackend.admin.entity.AdminRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * JPA 仓储：AdminRole 数据访问。
 */
public interface AdminRoleRepository extends JpaRepository<AdminRole, Long> {

    Optional<AdminRole> findByName(String name);

    boolean existsByName(String name);
}
