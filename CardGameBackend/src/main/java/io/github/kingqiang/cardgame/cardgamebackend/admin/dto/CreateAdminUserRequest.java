package io.github.kingqiang.cardgame.cardgamebackend.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * API 请求体：CreateAdminUser。
 */
@Getter
@Setter
public class CreateAdminUserRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String realName;

    @NotNull
    private Long roleId;
}
