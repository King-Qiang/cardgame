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
 * API 请求体：WalletAdjust。
 */
@Getter
@Setter
@Entity
@Table(name = "wallet_adjust_request")
public class WalletAdjustRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "adjust_type", nullable = false, length = 16)
    private String adjustType;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false, length = 256)
    private String reason;

    @Column(nullable = false, length = 16)
    private String status = "PENDING";

    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;

    @Column(name = "approver_id")
    private Long approverId;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "reject_reason", length = 256)
    private String rejectReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
