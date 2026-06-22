package io.github.kingqiang.cardgame.cardgamebackend.admin.controller;

import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageRequest;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageResult;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ApiResponse;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.TraceIdHolder;
import io.github.kingqiang.cardgame.cardgamebackend.rank.dto.PlayerRankDto;
import io.github.kingqiang.cardgame.cardgamebackend.rank.dto.PlayerRankLogDto;
import io.github.kingqiang.cardgame.cardgamebackend.rank.dto.RankLeaderboardItemDto;
import io.github.kingqiang.cardgame.cardgamebackend.rank.service.RankService;
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
 * 运营后台 REST API 控制器：AdminRank 相关接口。
 */
@Tag(name = "运营段位")
@RestController
@RequestMapping("/api/admin/v1/admin/rank")
@RequiredArgsConstructor
public class AdminRankController {

    private final RankService rankService;

    @Operation(summary = "段位排行榜")
    @GetMapping("/leaderboard")
    public ApiResponse<PageResult<RankLeaderboardItemDto>> leaderboard(
            @RequestParam(defaultValue = "DOUDIZHU") String gameType,
            @RequestParam(required = false) String seasonId,
            @RequestParam(required = false) String tier,
            PageRequest pageRequest) {
        return ApiResponse.ok(rankService.leaderboard(gameType, seasonId, tier, pageRequest))
                .withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "用户段位信息")
    @GetMapping("/users/{userId}")
    public ApiResponse<PlayerRankDto> userRank(
            @PathVariable long userId,
            @RequestParam(defaultValue = "DOUDIZHU") String gameType) {
        return ApiResponse.ok(rankService.getMyRank(userId, gameType)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "用户最近段位变动")
    @GetMapping("/users/{userId}/logs")
    public ApiResponse<List<PlayerRankLogDto>> userRankLogs(
            @PathVariable long userId,
            @RequestParam(defaultValue = "DOUDIZHU") String gameType) {
        return ApiResponse.ok(rankService.recentLogs(userId, gameType)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "当前赛季 ID")
    @GetMapping("/season")
    public ApiResponse<String> currentSeason() {
        return ApiResponse.ok(rankService.currentSeasonId()).withTraceId(TraceIdHolder.get());
    }
}
