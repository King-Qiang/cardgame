package io.github.kingqiang.cardgame.cardgamebackend.economy.controller;

import io.github.kingqiang.cardgame.cardgamebackend.auth.security.SecurityUtils;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ApiResponse;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.TraceIdHolder;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.CreateRechargeOrderRequest;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.RechargeOrderDto;
import io.github.kingqiang.cardgame.cardgamebackend.economy.service.RechargeOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 玩家 REST API 控制器：RechargeOrder 相关接口。
 */
@Tag(name = "玩家充值订单")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class RechargeOrderController {

    private final RechargeOrderService rechargeOrderService;

    @Operation(summary = "创建充值订单")
    @PostMapping
    public ApiResponse<RechargeOrderDto> create(@Valid @RequestBody CreateRechargeOrderRequest request) {
        long userId = SecurityUtils.requirePlayerId();
        return ApiResponse.ok(rechargeOrderService.createOrder(userId, request)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "支付回调（微信服务器 / 本地 mock）")
    @PostMapping("/{orderNo}/pay-callback")
    public ApiResponse<RechargeOrderDto> payCallback(@PathVariable String orderNo) {
        return ApiResponse.ok(rechargeOrderService.payCallback(orderNo)).withTraceId(TraceIdHolder.get());
    }
}
