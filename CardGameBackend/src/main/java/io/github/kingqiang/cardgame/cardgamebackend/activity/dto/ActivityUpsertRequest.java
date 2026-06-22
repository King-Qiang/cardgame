package io.github.kingqiang.cardgame.cardgamebackend.activity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * API 请求体：ActivityUpsert。
 */
@Getter
@Setter
public class ActivityUpsertRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @NotBlank
    private String type;

    @NotNull
    private Object configJson;

    @NotNull
    private Integer status;

    private LocalDateTime startAt;

    private LocalDateTime endAt;
}
