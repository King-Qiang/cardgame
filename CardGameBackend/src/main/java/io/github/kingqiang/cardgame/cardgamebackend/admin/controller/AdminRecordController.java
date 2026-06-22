package io.github.kingqiang.cardgame.cardgamebackend.admin.controller;

import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.AdminRecordDetailDto;
import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.RecordReplayMetaDto;
import io.github.kingqiang.cardgame.cardgamebackend.admin.service.AdminRecordService;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageRequest;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageResult;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ApiResponse;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.TraceIdHolder;
import io.github.kingqiang.cardgame.cardgamebackend.record.dto.GameActionLogDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 运营后台 REST API 控制器：AdminRecord 相关接口。
 */
@Tag(name = "运营对局记录")
@RestController
@RequestMapping("/api/admin/v1/admin/records")
@RequiredArgsConstructor
public class AdminRecordController {

    private final AdminRecordService adminRecordService;

    @Operation(summary = "对局列表")
    @GetMapping
    public ApiResponse<PageResult<AdminRecordService.AdminRecordListItemDto>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String gameType,
            @RequestParam(required = false) String mode,
            PageRequest pageRequest) {
        return ApiResponse.ok(adminRecordService.list(status, gameType, mode, pageRequest))
                .withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "对局详情")
    @GetMapping("/{recordId}")
    public ApiResponse<AdminRecordDetailDto> detail(@PathVariable String recordId) {
        return ApiResponse.ok(adminRecordService.detail(recordId)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "对局回放")
    @GetMapping("/{recordId}/replay")
    public ApiResponse<List<GameActionLogDto>> replay(@PathVariable String recordId) {
        return ApiResponse.ok(adminRecordService.replay(recordId)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "回放元数据")
    @GetMapping("/{recordId}/replay/meta")
    public ApiResponse<RecordReplayMetaDto> replayMeta(@PathVariable String recordId) {
        return ApiResponse.ok(adminRecordService.replayMeta(recordId)).withTraceId(TraceIdHolder.get());
    }
}
