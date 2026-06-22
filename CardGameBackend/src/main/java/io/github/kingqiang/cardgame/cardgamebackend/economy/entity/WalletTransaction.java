package io.github.kingqiang.cardgame.cardgamebackend.economy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA 实体，映射表 `wallet_transaction`。
 */
@Getter
@Setter
@Entity
@Table(name = "wallet_transaction")
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 32)
    private String type;

    @Column(nullable = false)
    private Long amount;

    @Column(name = "balance_after", nullable = false)
    private Long balanceAfter;

    @Column(name = "ref_type", nullable = false, length = 32)
    private String refType = "";

    @Column(name = "ref_id", nullable = false, length = 64)
    private String refId = "";

    @Column(nullable = false, length = 256)
    private String remark = "";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
