package io.github.kingqiang.cardgame.cardgamebackend.admin.controller;

import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.AdminAccountDto;
import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.CreateAdminUserRequest;
import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.ResetAdminPasswordRequest;
import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.UpdateAdminUserRequest;
import io.github.kingqiang.cardgame.cardgamebackend.admin.service.AdminAccountService;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageRequest;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageResult;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ApiResponse;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.TraceIdHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运营后台 REST API 控制器：AdminAccount 相关接口。
 */
@Tag(name = "管理员账号")
@RestController
@RequestMapping("/api/admin/v1/admin/admins")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    @Operation(summary = "管理员列表")
    @GetMapping
    public ApiResponse<PageResult<AdminAccountDto>> list(@RequestParam(required = false) String username,
                                                         @RequestParam(required = false) Integer status,
                                                         PageRequest pageRequest) {
        return ApiResponse.ok(adminAccountService.list(username, status, pageRequest)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "新增管理员")
    @PostMapping
    public ApiResponse<AdminAccountDto> create(@Valid @RequestBody CreateAdminUserRequest request) {
        return ApiResponse.ok(adminAccountService.create(request)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "更新管理员")
    @PutMapping("/{id}")
    public ApiResponse<AdminAccountDto> update(@PathVariable Long id, @Valid @RequestBody UpdateAdminUserRequest request) {
        return ApiResponse.ok(adminAccountService.update(id, request)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "重置密码")
    @PostMapping("/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetAdminPasswordRequest request) {
        adminAccountService.resetPassword(id, request);
        return ApiResponse.ok().withTraceId(TraceIdHolder.get());
    }
}
