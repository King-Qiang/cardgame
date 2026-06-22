package io.github.kingqiang.cardgame.cardgamebackend.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * API 请求体：UpdatePlayerProfile。
 */
@Getter
@Setter
public class UpdatePlayerProfileRequest {

    @Size(min = 1, max = 16, message = "昵称长度为 1-16 个字符")
    private String nickname;

    @Size(max = 512, message = "头像 URL 过长")
    private String avatar;
}
