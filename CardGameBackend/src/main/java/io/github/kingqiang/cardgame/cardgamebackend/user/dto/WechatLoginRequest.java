package io.github.kingqiang.cardgame.cardgamebackend.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * API 请求体：WechatLogin。
 */
@Getter
@Setter
public class WechatLoginRequest {

    @NotBlank
    private String code;
}
