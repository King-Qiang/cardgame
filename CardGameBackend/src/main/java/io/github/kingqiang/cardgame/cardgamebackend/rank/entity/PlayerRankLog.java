package io.github.kingqiang.cardgame.cardgamebackend.rank.entity;

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
 * JPA 实体，映射表 `player_rank_log`。
 */
@Getter
@Setter
@Entity
@Table(name = "player_rank_log")
public class PlayerRankLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "record_id", nullable = false, length = 32)
    private String recordId;

    @Column(name = "game_type", nullable = false, length = 32)
    private String gameType;

    @Column(name = "season_id", nullable = false, length = 16)
    private String seasonId;

    @Column(name = "delta_points", nullable = false)
    private Integer deltaPoints;

    @Column(name = "tier_before", nullable = false, length = 16)
    private String tierBefore;

    @Column(name = "tier_after", nullable = false, length = 16)
    private String tierAfter;

    @Column(name = "points_before", nullable = false)
    private Integer pointsBefore;

    @Column(name = "points_after", nullable = false)
    private Integer pointsAfter;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
