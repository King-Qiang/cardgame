package io.github.kingqiang.cardgame.cardgamebackend.activity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA 实体，映射表 `player_sign_record`。
 */
@Getter
@Setter
@Entity
@Table(name = "player_sign_record")
public class PlayerSignRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "sign_date", nullable = false)
    private LocalDate signDate;

    @Column(name = "streak_day", nullable = false)
    private Integer streakDay;

    @Column(name = "reward_gold", nullable = false)
    private Long rewardGold;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
