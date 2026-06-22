package io.github.kingqiang.cardgame.cardgamebackend.admin.controller;

import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.OperationLogDto;
import io.github.kingqiang.cardgame.cardgamebackend.admin.service.AdminOperationLogService;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageRequest;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageResult;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ApiResponse;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.TraceIdHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运营后台 REST API 控制器：AdminOperationLog 相关接口。
 */
@Tag(name = "操作日志")
@RestController
@RequestMapping("/api/admin/v1/admin/operation-logs")
@RequiredArgsConstructor
public class AdminOperationLogController {

    private final AdminOperationLogService adminOperationLogService;

    @Operation(summary = "操作日志列表")
    @GetMapping
    public ApiResponse<PageResult<OperationLogDto>> list(@RequestParam(required = false) Long operatorId,
                                                         @RequestParam(required = false) String action,
                                                         @RequestParam(required = false) String targetType,
                                                         PageRequest pageRequest) {
        return ApiResponse.ok(adminOperationLogService.list(operatorId, action, targetType, pageRequest))
                .withTraceId(TraceIdHolder.get());
    }
}
