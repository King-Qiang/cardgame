package io.github.kingqiang.cardgame.cardgamebackend.admin.repository;

import io.github.kingqiang.cardgame.cardgamebackend.admin.entity.AdminUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * JPA 仓储：AdminUser 数据访问。
 */
public interface AdminUserRepository extends JpaRepository<AdminUser, Long>, JpaSpecificationExecutor<AdminUser> {

    @EntityGraph(attributePaths = "role")
    Optional<AdminUser> findByUsername(String username);

    @EntityGraph(attributePaths = "role")
    Optional<AdminUser> findWithRoleById(Long id);

    boolean existsByUsername(String username);

    long countByRoleId(Long roleId);
}
