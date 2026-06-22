package io.github.kingqiang.cardgame.cardgamebackend.rank.controller;

import io.github.kingqiang.cardgame.cardgamebackend.auth.security.SecurityUtils;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageRequest;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageResult;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ApiResponse;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.TraceIdHolder;
import io.github.kingqiang.cardgame.cardgamebackend.rank.dto.PlayerRankDto;
import io.github.kingqiang.cardgame.cardgamebackend.rank.dto.RankLeaderboardItemDto;
import io.github.kingqiang.cardgame.cardgamebackend.rank.service.RankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 玩家 REST API 控制器：PlayerRank 相关接口。
 */
@Tag(name = "玩家段位")
@RestController
@RequestMapping("/api/v1/rank")
@RequiredArgsConstructor
public class PlayerRankController {

    private final RankService rankService;

    @Operation(summary = "我的段位")
    @GetMapping("/me")
    public ApiResponse<PlayerRankDto> myRank(@RequestParam(defaultValue = "DOUDIZHU") String gameType) {
        long userId = SecurityUtils.requirePlayerId();
        return ApiResponse.ok(rankService.getMyRank(userId, gameType)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "排行榜")
    @GetMapping("/leaderboard")
    public ApiResponse<PageResult<RankLeaderboardItemDto>> leaderboard(
            @RequestParam(defaultValue = "DOUDIZHU") String gameType,
            @RequestParam(required = false) String seasonId,
            PageRequest pageRequest) {
        return ApiResponse.ok(rankService.leaderboard(gameType, seasonId, null, pageRequest))
                .withTraceId(TraceIdHolder.get());
    }
}
