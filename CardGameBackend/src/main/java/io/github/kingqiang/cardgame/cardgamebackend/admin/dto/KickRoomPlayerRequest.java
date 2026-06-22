package io.github.kingqiang.cardgame.cardgamebackend.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * API 请求体：KickRoomPlayer。
 */
@Getter
@Setter
public class KickRoomPlayerRequest {

    @NotNull
    private Long userId;

    private String reason = "";
}
