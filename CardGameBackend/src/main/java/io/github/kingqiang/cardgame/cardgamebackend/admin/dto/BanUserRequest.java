package io.github.kingqiang.cardgame.cardgamebackend.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * API 请求体：BanUser。
 */
@Getter
@Setter
public class BanUserRequest {

    @NotBlank
    private String reason;

    private LocalDateTime banUntil;
}
