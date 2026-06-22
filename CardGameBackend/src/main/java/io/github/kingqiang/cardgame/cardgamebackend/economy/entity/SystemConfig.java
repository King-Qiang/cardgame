package io.github.kingqiang.cardgame.cardgamebackend.economy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * JPA 实体，映射表 `system_config`。
 */
@Getter
@Setter
@Entity
@Table(name = "system_config")
public class SystemConfig {

    @Id
    @Column(name = "config_key", length = 64)
    private String configKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config_value", nullable = false, columnDefinition = "json")
    private Object configValue;

    @Column(nullable = false, length = 256)
    private String description = "";

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
