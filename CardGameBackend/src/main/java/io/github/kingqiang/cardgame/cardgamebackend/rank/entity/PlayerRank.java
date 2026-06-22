package io.github.kingqiang.cardgame.cardgamebackend.rank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * JPA 实体，映射表 `player_rank`。
 */
@Getter
@Setter
@Entity
@Table(name = "player_rank")
@IdClass(PlayerRank.PlayerRankId.class)
public class PlayerRank {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "game_type", length = 32)
    private String gameType;

    @Id
    @Column(name = "season_id", length = 16)
    private String seasonId;

    @Column(nullable = false, length = 16)
    private String tier = "BRONZE";

    @Column(nullable = false)
    private Integer points = 0;

    @Column(nullable = false)
    private Integer wins = 0;

    @Column(nullable = false)
    private Integer losses = 0;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    public static class PlayerRankId implements Serializable {
        private Long userId;
        private String gameType;
        private String seasonId;
    }
}
