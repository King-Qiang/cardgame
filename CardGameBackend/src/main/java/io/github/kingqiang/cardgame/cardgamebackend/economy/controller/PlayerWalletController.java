package io.github.kingqiang.cardgame.cardgamebackend.economy.controller;

import io.github.kingqiang.cardgame.cardgamebackend.auth.security.SecurityUtils;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageRequest;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageResult;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ApiResponse;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.TraceIdHolder;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.CreateRechargeOrderRequest;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.RechargeOrderDto;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.ShopBuyRequest;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.ShopBuyResultDto;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.ShopItemDto;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.WalletBalanceDto;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.WalletTransactionDto;
import io.github.kingqiang.cardgame.cardgamebackend.economy.service.PlayerShopService;
import io.github.kingqiang.cardgame.cardgamebackend.economy.service.PlayerWalletService;
import io.github.kingqiang.cardgame.cardgamebackend.economy.service.RechargeOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 玩家 REST API 控制器：PlayerWallet 相关接口。
 */
@Tag(name = "玩家钱包")
@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class PlayerWalletController {

    private final PlayerWalletService playerWalletService;

    @Operation(summary = "钱包余额")
    @GetMapping
    public ApiResponse<WalletBalanceDto> balance() {
        long userId = SecurityUtils.requirePlayerId();
        return ApiResponse.ok(playerWalletService.getBalance(userId)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "金币流水")
    @GetMapping("/transactions")
    public ApiResponse<PageResult<WalletTransactionDto>> transactions(PageRequest pageRequest) {
        long userId = SecurityUtils.requirePlayerId();
        return ApiResponse.ok(playerWalletService.listTransactions(userId, pageRequest))
                .withTraceId(TraceIdHolder.get());
    }
}
