package io.github.kingqiang.cardgame.cardgamebackend.admin.controller;

import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.DashboardAlertsResponse;
import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.DashboardOverviewResponse;
import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.DashboardTrendsResponse;
import io.github.kingqiang.cardgame.cardgamebackend.admin.service.AdminDashboardService;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ApiResponse;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.TraceIdHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运营后台 REST API 控制器：AdminDashboard 相关接口。
 */
@Tag(name = "运营仪表盘")
@RestController
@RequestMapping("/api/admin/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @Operation(summary = "核心指标概览")
    @GetMapping("/overview")
    public ApiResponse<DashboardOverviewResponse> overview() {
        return ApiResponse.ok(adminDashboardService.overview()).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "近 N 日趋势")
    @GetMapping("/trends")
    public ApiResponse<DashboardTrendsResponse> trends(@org.springframework.web.bind.annotation.RequestParam(defaultValue = "7") int days) {
        return ApiResponse.ok(adminDashboardService.trends(days)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "仪表盘告警")
    @GetMapping("/alerts")
    public ApiResponse<DashboardAlertsResponse> alerts() {
        return ApiResponse.ok(adminDashboardService.alerts()).withTraceId(TraceIdHolder.get());
    }
}
