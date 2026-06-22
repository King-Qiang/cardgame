package io.github.kingqiang.cardgame.cardgamebackend.admin.controller;

import io.github.kingqiang.cardgame.cardgamebackend.admin.service.AdminWalletService;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageRequest;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageResult;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ApiResponse;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.TraceIdHolder;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.CreateAdjustRequestRequest;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.RejectAdjustRequest;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.WalletAdjustRequestDto;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.WalletTransactionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运营后台 REST API 控制器：AdminWallet 相关接口。
 */
@Tag(name = "运营钱包管理")
@RestController
@RequestMapping("/api/admin/v1/admin/wallet")
@RequiredArgsConstructor
public class AdminWalletController {

    private final AdminWalletService adminWalletService;

    @Operation(summary = "金币流水分页查询")
    @GetMapping("/transactions")
    public ApiResponse<PageResult<WalletTransactionDto>> listTransactions(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String type,
            PageRequest pageRequest) {
        return ApiResponse.ok(adminWalletService.listTransactions(userId, type, pageRequest))
                .withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "提交大额调账申请")
    @PostMapping("/adjust-requests")
    public ApiResponse<WalletAdjustRequestDto> createAdjustRequest(
            @Valid @RequestBody CreateAdjustRequestRequest request) {
        return ApiResponse.ok(adminWalletService.createAdjustRequest(request))
                .withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "调账申请列表")
    @GetMapping("/adjust-requests")
    public ApiResponse<PageResult<WalletAdjustRequestDto>> listAdjustRequests(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            PageRequest pageRequest) {
        return ApiResponse.ok(adminWalletService.listAdjustRequests(userId, status, pageRequest))
                .withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "审批通过并执行调账")
    @PostMapping("/adjust-requests/{id}/approve")
    public ApiResponse<WalletAdjustRequestDto> approve(@PathVariable Long id) {
        return ApiResponse.ok(adminWalletService.approveAdjustRequest(id))
                .withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "驳回调账申请")
    @PostMapping("/adjust-requests/{id}/reject")
    public ApiResponse<WalletAdjustRequestDto> reject(@PathVariable Long id,
                                                      @Valid @RequestBody RejectAdjustRequest request) {
        return ApiResponse.ok(adminWalletService.rejectAdjustRequest(id, request))
                .withTraceId(TraceIdHolder.get());
    }
}
