package io.github.kingqiang.cardgame.cardgamebackend.room.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * API 请求体：CreatePveRoom。
 */
@Getter
@Setter
public class CreatePveRoomRequest {

    @NotBlank
    private String gameType;

    private Map<String, Object> config;
}
