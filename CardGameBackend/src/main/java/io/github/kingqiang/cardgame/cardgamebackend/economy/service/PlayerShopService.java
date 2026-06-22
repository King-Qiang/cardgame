package io.github.kingqiang.cardgame.cardgamebackend.economy.service;

import io.github.kingqiang.cardgame.cardgamebackend.common.exception.BusinessException;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.ShopBuyRequest;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.ShopBuyResultDto;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.ShopItemDto;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.WalletChangeResult;
import io.github.kingqiang.cardgame.cardgamebackend.economy.entity.ShopItem;
import io.github.kingqiang.cardgame.cardgamebackend.economy.enums.WalletTransactionType;
import io.github.kingqiang.cardgame.cardgamebackend.economy.repository.ShopItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 业务服务：PlayerShop 领域逻辑。
 */
@Service
@RequiredArgsConstructor
public class PlayerShopService {

    private final ShopItemRepository shopItemRepository;
    private final WalletService walletService;

    @Transactional(readOnly = true)
    public List<ShopItemDto> listOnSaleItems() {
        return shopItemRepository.findByStatusOrderBySortOrderAsc(1).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ShopBuyResultDto buy(long userId, ShopBuyRequest request) {
        ShopItem item = shopItemRepository.findById(request.getItemId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SHOP_ITEM_NOT_FOUND));
        if (item.getStatus() == null || item.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "商品已下架");
        }
        long totalCost = item.getPrice() * request.getQuantity();
        long grantedGold = extractGrantedGold(item.getPayload()) * request.getQuantity();
        WalletChangeResult deductResult = walletService.changeGold(
                userId,
                -totalCost,
                WalletTransactionType.SHOP_BUY,
                "SHOP_ITEM",
                String.valueOf(item.getId()),
                "购买商品：" + item.getName()
        );
        long balanceAfter = deductResult.getBalanceAfter();
        if (grantedGold > 0) {
            WalletChangeResult grantResult = walletService.changeGold(
                    userId,
                    grantedGold,
                    WalletTransactionType.SHOP_BUY,
                    "SHOP_ITEM",
                    String.valueOf(item.getId()),
                    "商品到账金币"
            );
            balanceAfter = grantResult.getBalanceAfter();
        }
        return ShopBuyResultDto.builder()
                .itemId(item.getId())
                .quantity(request.getQuantity())
                .totalCost(totalCost)
                .balanceAfter(balanceAfter)
                .grantedGold(grantedGold)
                .build();
    }

    private long extractGrantedGold(Object payload) {
        if (payload instanceof Map<?, ?> map) {
            Object gold = map.get("gold");
            if (gold instanceof Number number) {
                return number.longValue();
            }
        }
        return 0L;
    }

    private ShopItemDto toDto(ShopItem item) {
        return ShopItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .price(item.getPrice())
                .currency(item.getCurrency())
                .payload(item.getPayload())
                .status(item.getStatus())
                .statusLabel(item.getStatus() != null && item.getStatus() == 1 ? "上架" : "下架")
                .sortOrder(item.getSortOrder())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
