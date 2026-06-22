package io.github.kingqiang.cardgame.cardgamebackend.user.repository;

import io.github.kingqiang.cardgame.cardgamebackend.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA 仓储：UserProfile 数据访问。
 */
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
