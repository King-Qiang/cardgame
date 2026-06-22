package io.github.kingqiang.cardgame.cardgamebackend.match;

import io.github.kingqiang.cardgame.cardgamebackend.common.response.ApiResponse;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.TraceIdHolder;
import io.github.kingqiang.cardgame.cardgamebackend.match.dto.QuickMatchRequest;
import io.github.kingqiang.cardgame.cardgamebackend.match.dto.QuickMatchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 玩家 REST API 控制器：Match 相关接口。
 */
@Tag(name = "快速匹配")
@RestController
@RequestMapping("/api/v1/match")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @Operation(summary = "加入快速匹配队列")
    @PostMapping("/quick")
    public ApiResponse<QuickMatchResponse> quickMatch(@Valid @RequestBody QuickMatchRequest request) {
        return ApiResponse.ok(matchService.quickMatch(request)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "取消快速匹配")
    @DeleteMapping("/quick")
    public ApiResponse<QuickMatchResponse> cancel(@RequestParam(defaultValue = "DOUDIZHU") String gameType) {
        return ApiResponse.ok(matchService.cancelQuickMatch(gameType)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "查询匹配状态（排队轮询）")
    @GetMapping("/status")
    public ApiResponse<QuickMatchResponse> status(@RequestParam(defaultValue = "DOUDIZHU") String gameType) {
        return ApiResponse.ok(matchService.getMatchStatus(gameType)).withTraceId(TraceIdHolder.get());
    }
}
