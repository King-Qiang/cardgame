package io.github.kingqiang.cardgame.cardgamebackend.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * API 请求体：ResetAdminPassword。
 */
@Getter
@Setter
public class ResetAdminPasswordRequest {

    @NotBlank
    private String newPassword;
}
