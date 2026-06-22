package io.github.kingqiang.cardgame.cardgamebackend.admin.controller;

import io.github.kingqiang.cardgame.cardgamebackend.admin.service.AdminShopService;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageRequest;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageResult;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ApiResponse;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.TraceIdHolder;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.ShopItemDto;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.ShopItemUpsertRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运营后台 REST API 控制器：AdminShop 相关接口。
 */
@Tag(name = "商城管理")
@RestController
@RequestMapping("/api/admin/v1/admin/shop/items")
@RequiredArgsConstructor
public class AdminShopController {

    private final AdminShopService adminShopService;

    @Operation(summary = "商品列表")
    @GetMapping
    public ApiResponse<PageResult<ShopItemDto>> list(@RequestParam(required = false) Integer status,
                                                     PageRequest pageRequest) {
        return ApiResponse.ok(adminShopService.list(status, pageRequest)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "商品详情")
    @GetMapping("/{id}")
    public ApiResponse<ShopItemDto> get(@PathVariable Long id) {
        return ApiResponse.ok(adminShopService.get(id)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "新增商品")
    @PostMapping
    public ApiResponse<ShopItemDto> create(@Valid @RequestBody ShopItemUpsertRequest request) {
        return ApiResponse.ok(adminShopService.create(request)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "更新商品")
    @PutMapping("/{id}")
    public ApiResponse<ShopItemDto> update(@PathVariable Long id, @Valid @RequestBody ShopItemUpsertRequest request) {
        return ApiResponse.ok(adminShopService.update(id, request)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminShopService.delete(id);
        return ApiResponse.ok().withTraceId(TraceIdHolder.get());
    }
}
