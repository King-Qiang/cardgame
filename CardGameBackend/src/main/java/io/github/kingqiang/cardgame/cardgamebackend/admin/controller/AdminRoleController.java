package io.github.kingqiang.cardgame.cardgamebackend.admin.controller;

import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.AdminRoleDto;
import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.AdminRoleUpsertRequest;
import io.github.kingqiang.cardgame.cardgamebackend.admin.service.AdminRoleService;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ApiResponse;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.TraceIdHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 运营后台 REST API 控制器：AdminRole 相关接口。
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/api/admin/v1/admin/roles")
@RequiredArgsConstructor
public class AdminRoleController {

    private final AdminRoleService adminRoleService;

    @Operation(summary = "角色列表")
    @GetMapping
    public ApiResponse<List<AdminRoleDto>> list() {
        return ApiResponse.ok(adminRoleService.list()).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "角色详情")
    @GetMapping("/{id}")
    public ApiResponse<AdminRoleDto> get(@PathVariable Long id) {
        return ApiResponse.ok(adminRoleService.get(id)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "新增角色")
    @PostMapping
    public ApiResponse<AdminRoleDto> create(@Valid @RequestBody AdminRoleUpsertRequest request) {
        return ApiResponse.ok(adminRoleService.create(request)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    public ApiResponse<AdminRoleDto> update(@PathVariable Long id, @Valid @RequestBody AdminRoleUpsertRequest request) {
        return ApiResponse.ok(adminRoleService.update(id, request)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminRoleService.delete(id);
        return ApiResponse.ok().withTraceId(TraceIdHolder.get());
    }
}
