package io.github.kingqiang.cardgame.cardgamebackend.admin.controller;

import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.AdminUserListItemDto;
import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.BanUserRequest;
import io.github.kingqiang.cardgame.cardgamebackend.admin.service.AdminUserService;
import io.github.kingqiang.cardgame.cardgamebackend.admin.service.AdminWalletService;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageRequest;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageResult;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ApiResponse;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.TraceIdHolder;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.AdjustWalletRequest;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.WalletChangeResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运营后台 REST API 控制器：AdminUser 相关接口。
 */
@Tag(name = "运营用户管理")
@RestController
@RequestMapping("/api/admin/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final AdminWalletService adminWalletService;

    @Operation(summary = "用户列表")
    @GetMapping
    public ApiResponse<PageResult<AdminUserListItemDto>> list(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String status,
            PageRequest pageRequest) {
        return ApiResponse.ok(adminUserService.list(userId, nickname, status, pageRequest))
                .withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "封禁用户")
    @PostMapping("/{id}/ban")
    public ApiResponse<Void> ban(@PathVariable Long id, @Valid @RequestBody BanUserRequest request) {
        adminUserService.banUser(id, request);
        return ApiResponse.ok().withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "解封用户")
    @PostMapping("/{id}/unban")
    public ApiResponse<Void> unban(@PathVariable Long id) {
        adminUserService.unbanUser(id);
        return ApiResponse.ok().withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "小额直接调账")
    @PostMapping("/{id}/adjust-wallet")
    public ApiResponse<WalletChangeResult> adjustWallet(@PathVariable Long id,
                                                        @Valid @RequestBody AdjustWalletRequest request) {
        return ApiResponse.ok(adminWalletService.adjustWalletDirect(id, request))
                .withTraceId(TraceIdHolder.get());
    }
}
