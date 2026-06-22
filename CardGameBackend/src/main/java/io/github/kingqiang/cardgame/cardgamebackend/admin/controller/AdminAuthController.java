package io.github.kingqiang.cardgame.cardgamebackend.admin.controller;

import io.github.kingqiang.cardgame.cardgamebackend.auth.dto.RefreshTokenRequest;
import io.github.kingqiang.cardgame.cardgamebackend.auth.dto.TokenPairResponse;
import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.AdminLoginRequest;
import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.AdminLoginResponse;
import io.github.kingqiang.cardgame.cardgamebackend.admin.service.AdminAuthService;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ApiResponse;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.TraceIdHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运营后台 REST API 控制器：AdminAuth 相关接口。
 */
@RestController
@RequestMapping("/api/admin/v1/admin/auth")
@RequiredArgsConstructor
@Tag(name = "运营认证")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request,
                                                 HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getRemoteAddr();
        AdminLoginResponse response = adminAuthService.login(request, clientIp);
        return ApiResponse.ok(response).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "管理员登出")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.ok().withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "刷新 access token")
    @PostMapping("/refresh")
    public ApiResponse<TokenPairResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        TokenPairResponse response = adminAuthService.refresh(request.getRefreshToken());
        return ApiResponse.ok(response).withTraceId(TraceIdHolder.get());
    }
}
