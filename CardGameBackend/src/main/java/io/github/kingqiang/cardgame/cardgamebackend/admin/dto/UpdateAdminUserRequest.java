package io.github.kingqiang.cardgame.cardgamebackend.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * API 请求体：UpdateAdminUser。
 */
@Getter
@Setter
public class UpdateAdminUserRequest {

    @NotNull
    private Long roleId;

    @NotNull
    private Integer status;

    private String realName;
}
