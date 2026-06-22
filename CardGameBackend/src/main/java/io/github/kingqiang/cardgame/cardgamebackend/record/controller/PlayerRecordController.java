package io.github.kingqiang.cardgame.cardgamebackend.record.controller;

import io.github.kingqiang.cardgame.cardgamebackend.auth.security.SecurityUtils;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageRequest;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageResult;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ApiResponse;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.TraceIdHolder;
import io.github.kingqiang.cardgame.cardgamebackend.record.dto.GameActionLogDto;
import io.github.kingqiang.cardgame.cardgamebackend.record.dto.PlayerRecordDetailDto;
import io.github.kingqiang.cardgame.cardgamebackend.record.dto.PlayerRecordListItemDto;
import io.github.kingqiang.cardgame.cardgamebackend.record.service.PlayerRecordService;
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
 * 玩家 REST API 控制器：PlayerRecord 相关接口。
 */
@Tag(name = "玩家战绩")
@RestController
@RequestMapping("/api/v1/records")
@RequiredArgsConstructor
public class PlayerRecordController {

    private final PlayerRecordService playerRecordService;

    @Operation(summary = "本人历史战绩")
    @GetMapping
    public ApiResponse<PageResult<PlayerRecordListItemDto>> list(
            @RequestParam(required = false) String gameType,
            @RequestParam(required = false) String mode,
            PageRequest pageRequest) {
        long userId = SecurityUtils.requirePlayerId();
        return ApiResponse.ok(playerRecordService.list(userId, gameType, mode, pageRequest))
                .withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "对局详情")
    @GetMapping("/{recordId}")
    public ApiResponse<PlayerRecordDetailDto> detail(@PathVariable String recordId) {
        long userId = SecurityUtils.requirePlayerId();
        return ApiResponse.ok(playerRecordService.detail(recordId, userId))
                .withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "对局回放")
    @GetMapping("/{recordId}/replay")
    public ApiResponse<List<GameActionLogDto>> replay(@PathVariable String recordId) {
        long userId = SecurityUtils.requirePlayerId();
        return ApiResponse.ok(playerRecordService.replay(recordId, userId))
                .withTraceId(TraceIdHolder.get());
    }
}
