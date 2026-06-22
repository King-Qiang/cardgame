package io.github.kingqiang.cardgame.cardgamebackend.room.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * API 请求体：CreateRoom。
 */
@Getter
@Setter
public class CreateRoomRequest {

    @NotBlank
    private String gameType = "DOUDIZHU";

    @NotBlank
    private String mode = "FRIEND";

    @NotNull
    private Map<String, Object> config = Map.of("baseScore", 1, "enableGrab", true, "enableDouble", true);
}
