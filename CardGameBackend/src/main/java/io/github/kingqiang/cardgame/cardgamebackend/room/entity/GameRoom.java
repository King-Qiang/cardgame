package io.github.kingqiang.cardgame.cardgamebackend.room.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * JPA 实体，映射表 `game_room`。
 */
@Getter
@Setter
@Entity
@Table(name = "game_room")
public class GameRoom {

    @Id
    @Column(name = "room_id", length = 32)
    private String roomId;

    @Column(name = "game_type", nullable = false, length = 32)
    private String gameType;

    @Column(nullable = false, length = 32)
    private String mode;

    @Column(nullable = false, length = 16)
    private String status = "WAITING";

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "max_players", nullable = false)
    private Integer maxPlayers = 3;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config_json", columnDefinition = "json")
    private Map<String, Object> configJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
