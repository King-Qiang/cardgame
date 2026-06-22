package io.github.kingqiang.cardgame.cardgamebackend.admin.controller;

import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.OperationActions;
import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.service.OperationLogService;
import io.github.kingqiang.cardgame.cardgamebackend.auth.security.SecurityUtils;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ApiResponse;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.RequestUtils;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.TraceIdHolder;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.SystemConfigDto;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.UpdateSystemConfigRequest;
import io.github.kingqiang.cardgame.cardgamebackend.economy.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 运营后台 REST API 控制器：AdminSystemConfig 相关接口。
 */
@Tag(name = "系统配置")
@RestController
@RequestMapping("/api/admin/v1/admin/system/configs")
@RequiredArgsConstructor
public class AdminSystemConfigController {

    private final SystemConfigService systemConfigService;
    private final OperationLogService operationLogService;

    @Operation(summary = "系统配置列表")
    @GetMapping
    public ApiResponse<List<SystemConfigDto>> list() {
        return ApiResponse.ok(systemConfigService.listAll()).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "读取单个配置")
    @GetMapping("/{key:.+}")
    public ApiResponse<SystemConfigDto> get(@PathVariable String key) {
        return ApiResponse.ok(systemConfigService.getByKey(key)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "更新单个配置")
    @PutMapping("/{key:.+}")
    public ApiResponse<SystemConfigDto> update(@PathVariable String key,
                                               @Valid @RequestBody UpdateSystemConfigRequest request) {
        SystemConfigDto updated = systemConfigService.update(key, request);
        operationLogService.log(
                SecurityUtils.requireAdmin().getId(),
                OperationActions.CONFIG_UPDATE,
                "CONFIG",
                key,
                Map.of("configValue", request.getConfigValue()),
                RequestUtils.clientIp()
        );
        return ApiResponse.ok(updated).withTraceId(TraceIdHolder.get());
    }
}
