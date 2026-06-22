package io.github.kingqiang.cardgame.cardgamebackend.admin.service;

import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.OperationActions;
import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.service.OperationLogService;
import io.github.kingqiang.cardgame.cardgamebackend.auth.security.SecurityUtils;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageRequest;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageResult;
import io.github.kingqiang.cardgame.cardgamebackend.common.exception.BusinessException;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.RequestUtils;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.ShopItemDto;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.ShopItemUpsertRequest;
import io.github.kingqiang.cardgame.cardgamebackend.economy.entity.ShopItem;
import io.github.kingqiang.cardgame.cardgamebackend.economy.repository.ShopItemRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 业务服务：AdminShop 领域逻辑。
 */
@Service
@RequiredArgsConstructor
public class AdminShopService {

    private final ShopItemRepository shopItemRepository;
    private final OperationLogService operationLogService;

    @Transactional(readOnly = true)
    public PageResult<ShopItemDto> list(Integer status, PageRequest pageRequest) {
        Specification<ShopItem> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<ShopItem> page = shopItemRepository.findAll(spec,
                org.springframework.data.domain.PageRequest.of(
                        pageRequest.getPage() - 1,
                        pageRequest.getPageSize(),
                        Sort.by(Sort.Direction.ASC, "sortOrder", "id")));
        List<ShopItemDto> list = page.getContent().stream().map(this::toDto).toList();
        return PageResult.of(list, page.getTotalElements(), pageRequest.getPage(), pageRequest.getPageSize());
    }

    @Transactional(readOnly = true)
    public ShopItemDto get(Long id) {
        return toDto(findItem(id));
    }

    @Transactional
    public ShopItemDto create(ShopItemUpsertRequest request) {
        validateCurrency(request.getCurrency());
        LocalDateTime now = LocalDateTime.now();
        ShopItem item = new ShopItem();
        applyRequest(item, request);
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        ShopItem saved = shopItemRepository.save(item);
        log(OperationActions.SHOP_ITEM_CREATE, saved.getId(), Map.of("name", saved.getName()));
        return toDto(saved);
    }

    @Transactional
    public ShopItemDto update(Long id, ShopItemUpsertRequest request) {
        validateCurrency(request.getCurrency());
        ShopItem item = findItem(id);
        applyRequest(item, request);
        item.setUpdatedAt(LocalDateTime.now());
        ShopItem saved = shopItemRepository.save(item);
        log(OperationActions.SHOP_ITEM_UPDATE, saved.getId(), Map.of("name", saved.getName()));
        return toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        ShopItem item = findItem(id);
        shopItemRepository.delete(item);
        log(OperationActions.SHOP_ITEM_DELETE, id, Map.of("name", item.getName()));
    }

    private ShopItem findItem(Long id) {
        return shopItemRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHOP_ITEM_NOT_FOUND));
    }

    private void applyRequest(ShopItem item, ShopItemUpsertRequest request) {
        item.setName(request.getName());
        item.setPrice(request.getPrice());
        item.setCurrency(request.getCurrency());
        item.setPayload(request.getPayload());
        item.setStatus(request.getStatus());
        item.setSortOrder(request.getSortOrder());
    }

    private void validateCurrency(String currency) {
        if (!"GOLD".equals(currency) && !"DIAMOND".equals(currency)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "currency 必须为 GOLD 或 DIAMOND");
        }
    }

    private void log(String action, Long id, Map<String, Object> detail) {
        operationLogService.log(
                SecurityUtils.requireAdmin().getId(),
                action,
                "SHOP_ITEM",
                String.valueOf(id),
                detail,
                RequestUtils.clientIp()
        );
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
