package io.github.kingqiang.cardgame.cardgamebackend.activity.controller;

import io.github.kingqiang.cardgame.cardgamebackend.activity.dto.DailySignStatusDto;
import io.github.kingqiang.cardgame.cardgamebackend.activity.service.DailySignService;
import io.github.kingqiang.cardgame.cardgamebackend.auth.security.SecurityUtils;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ApiResponse;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.TraceIdHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 玩家 REST API 控制器：Activity 相关接口。
 */
@Tag(name = "玩家活动")
@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final DailySignService dailySignService;

    @Operation(summary = "每日签到状态")
    @GetMapping("/daily-sign")
    public ApiResponse<DailySignStatusDto> dailySignStatus() {
        long userId = SecurityUtils.requirePlayerId();
        return ApiResponse.ok(dailySignService.getStatus(userId)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "执行每日签到")
    @PostMapping("/daily-sign")
    public ApiResponse<DailySignStatusDto> dailySign() {
        long userId = SecurityUtils.requirePlayerId();
        return ApiResponse.ok(dailySignService.sign(userId)).withTraceId(TraceIdHolder.get());
    }
}
