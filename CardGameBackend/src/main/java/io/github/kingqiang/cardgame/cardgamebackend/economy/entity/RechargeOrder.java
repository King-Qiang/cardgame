package io.github.kingqiang.cardgame.cardgamebackend.economy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA 实体，映射表 `recharge_order`。
 */
@Getter
@Setter
@Entity
@Table(name = "recharge_order")
public class RechargeOrder {

    @Id
    @Column(name = "order_no", length = 64)
    private String orderNo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long amount;

    @Column(name = "gold_amount", nullable = false)
    private Long goldAmount;

    @Column(name = "pay_channel", nullable = false, length = 32)
    private String payChannel;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
