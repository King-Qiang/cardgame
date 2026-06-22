package io.github.kingqiang.cardgame.cardgamebackend.room.entity;

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
 * JPA 实体，映射表 `room_player`。
 */
@Getter
@Setter
@Entity
@Table(name = "room_player")
public class RoomPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false, length = 32)
    private String roomId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer seat;

    @Column(nullable = false)
    private Boolean ready = false;

    @Column(name = "is_robot", nullable = false)
    private Boolean isRobot = false;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;
}
