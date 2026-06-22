package io.github.kingqiang.cardgame.cardgamebackend.match.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * API 请求体：QuickMatch。
 */
@Getter
@Setter
public class QuickMatchRequest {

    @NotBlank
    private String gameType = "DOUDIZHU";

    /** MATCH = FIFO；RANKED = 段位撮合 */
    private String mode = "MATCH";
}
