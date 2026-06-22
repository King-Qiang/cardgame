package io.github.kingqiang.cardgame.cardgamebackend.admin.controller;

import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageRequest;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageResult;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ApiResponse;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.TraceIdHolder;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.RechargeOrderDto;
import io.github.kingqiang.cardgame.cardgamebackend.economy.service.RechargeOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运营后台 REST API 控制器：AdminOrder 相关接口。
 */
@Tag(name = "运营充值订单")
@RestController
@RequestMapping("/api/admin/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final RechargeOrderService rechargeOrderService;

    @Operation(summary = "充值订单列表")
    @GetMapping
    public ApiResponse<PageResult<RechargeOrderDto>> list(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            PageRequest pageRequest) {
        return ApiResponse.ok(rechargeOrderService.listOrders(userId, status, pageRequest))
                .withTraceId(TraceIdHolder.get());
    }
}
