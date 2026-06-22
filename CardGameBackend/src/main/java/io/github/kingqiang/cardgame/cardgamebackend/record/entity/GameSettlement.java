package io.github.kingqiang.cardgame.cardgamebackend.record.entity;

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
 * JPA 实体，映射表 `game_settlement`。
 */
@Getter
@Setter
@Entity
@Table(name = "game_settlement")
public class GameSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_id", nullable = false, length = 32)
    private String recordId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "gold_delta", nullable = false)
    private Long goldDelta;

    @Column(nullable = false)
    private Integer score = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
