package io.github.kingqiang.cardgame.cardgamebackend.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA 实体，映射表 `user_wallet`。
 */
@Getter
@Setter
@Entity
@Table(name = "user_wallet")
public class UserWallet {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private Long gold = 0L;

    @Column(nullable = false)
    private Long diamond = 0L;

    @Column(name = "frozen_gold", nullable = false)
    private Long frozenGold = 0L;

    @Version
    @Column(nullable = false)
    private Integer version = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
