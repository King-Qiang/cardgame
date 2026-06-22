package io.github.kingqiang.cardgame.cardgamebackend.economy.controller;

import io.github.kingqiang.cardgame.cardgamebackend.auth.security.SecurityUtils;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ApiResponse;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.TraceIdHolder;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.ShopBuyRequest;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.ShopBuyResultDto;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.ShopItemDto;
import io.github.kingqiang.cardgame.cardgamebackend.economy.service.PlayerShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 玩家 REST API 控制器：PlayerShop 相关接口。
 */
@Tag(name = "玩家商城")
@RestController
@RequestMapping("/api/v1/shop")
@RequiredArgsConstructor
public class PlayerShopController {

    private final PlayerShopService playerShopService;

    @Operation(summary = "商城商品列表")
    @GetMapping("/items")
    public ApiResponse<List<ShopItemDto>> listItems() {
        return ApiResponse.ok(playerShopService.listOnSaleItems()).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "购买商品")
    @PostMapping("/buy")
    public ApiResponse<ShopBuyResultDto> buy(@Valid @RequestBody ShopBuyRequest request) {
        long userId = SecurityUtils.requirePlayerId();
        return ApiResponse.ok(playerShopService.buy(userId, request)).withTraceId(TraceIdHolder.get());
    }
}
