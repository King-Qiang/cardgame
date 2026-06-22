package io.github.kingqiang.cardgame.cardgamebackend.user.controller;

import io.github.kingqiang.cardgame.cardgamebackend.auth.security.SecurityUtils;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ApiResponse;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.TraceIdHolder;
import io.github.kingqiang.cardgame.cardgamebackend.user.dto.PlayerMeDto;
import io.github.kingqiang.cardgame.cardgamebackend.user.dto.UpdatePlayerProfileRequest;
import io.github.kingqiang.cardgame.cardgamebackend.user.service.PlayerUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 玩家 REST API 控制器：PlayerUser 相关接口。
 */
@Tag(name = "玩家用户")
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class PlayerUserController {

    private final PlayerUserService playerUserService;

    @Operation(summary = "当前用户信息（含钱包与段位摘要）")
    @GetMapping("/me")
    public ApiResponse<PlayerMeDto> me(@RequestParam(defaultValue = "DOUDIZHU") String gameType) {
        long userId = SecurityUtils.requirePlayerId();
        return ApiResponse.ok(playerUserService.getMe(userId, gameType)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "更新昵称/头像")
    @PutMapping("/profile")
    public ApiResponse<PlayerMeDto> updateProfile(
            @Valid @RequestBody UpdatePlayerProfileRequest request,
            @RequestParam(defaultValue = "DOUDIZHU") String gameType) {
        long userId = SecurityUtils.requirePlayerId();
        return ApiResponse.ok(playerUserService.updateProfile(userId, request, gameType))
                .withTraceId(TraceIdHolder.get());
    }
}
