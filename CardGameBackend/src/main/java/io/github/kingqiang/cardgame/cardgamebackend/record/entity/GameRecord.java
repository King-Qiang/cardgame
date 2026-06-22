package io.github.kingqiang.cardgame.cardgamebackend.record.entity;

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
 * JPA 实体，映射表 `game_record`。
 */
@Getter
@Setter
@Entity
@Table(name = "game_record")
public class GameRecord {

    @Id
    @Column(name = "record_id", length = 32)
    private String recordId;

    @Column(name = "room_id", nullable = false, length = 32)
    private String roomId;

    @Column(name = "game_type", nullable = false, length = 32)
    private String gameType;

    @Column(nullable = false, length = 16)
    private String status = "PLAYING";

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result_json", columnDefinition = "json")
    private Map<String, Object> resultJson;
}
