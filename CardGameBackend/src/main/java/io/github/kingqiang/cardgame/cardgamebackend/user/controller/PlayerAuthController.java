package io.github.kingqiang.cardgame.cardgamebackend.user.controller;

import io.github.kingqiang.cardgame.cardgamebackend.auth.dto.RefreshTokenRequest;
import io.github.kingqiang.cardgame.cardgamebackend.auth.dto.TokenPairResponse;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ApiResponse;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.TraceIdHolder;
import io.github.kingqiang.cardgame.cardgamebackend.user.dto.PlayerLoginResponse;
import io.github.kingqiang.cardgame.cardgamebackend.user.dto.WechatLoginRequest;
import io.github.kingqiang.cardgame.cardgamebackend.user.service.PlayerAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 玩家 REST API 控制器：PlayerAuth 相关接口。
 */
@Tag(name = "玩家认证")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class PlayerAuthController {

    private final PlayerAuthService playerAuthService;

    @Operation(summary = "微信 code 登录")
    @PostMapping("/wechat/login")
    public ApiResponse<PlayerLoginResponse> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        PlayerLoginResponse response = playerAuthService.loginByWechat(request.getCode());
        return ApiResponse.ok(response).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "刷新 access token")
    @PostMapping("/refresh")
    public ApiResponse<TokenPairResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        TokenPairResponse response = playerAuthService.refresh(request.getRefreshToken());
        return ApiResponse.ok(response).withTraceId(TraceIdHolder.get());
    }
}
