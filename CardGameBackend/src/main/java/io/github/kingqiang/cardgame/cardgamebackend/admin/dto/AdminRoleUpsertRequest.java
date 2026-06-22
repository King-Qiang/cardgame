package io.github.kingqiang.cardgame.cardgamebackend.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * API 请求体：AdminRoleUpsert。
 */
@Getter
@Setter
public class AdminRoleUpsertRequest {

    @NotBlank
    private String name;

    private String description = "";

    @NotEmpty
    private List<String> permissions;
}
