package io.github.kingqiang.cardgame.cardgamebackend.economy.service;

import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageRequest;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageResult;
import io.github.kingqiang.cardgame.cardgamebackend.common.exception.BusinessException;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.BusinessIdGenerator;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.CreateRechargeOrderRequest;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.RechargeOrderDto;
import io.github.kingqiang.cardgame.cardgamebackend.economy.entity.RechargeOrder;
import io.github.kingqiang.cardgame.cardgamebackend.economy.enums.WalletTransactionType;
import io.github.kingqiang.cardgame.cardgamebackend.economy.repository.RechargeOrderRepository;
import io.github.kingqiang.cardgame.cardgamebackend.user.repository.PlayerRepository;
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

/**
 * 业务服务：RechargeOrder 领域逻辑。
 */
@Service
@RequiredArgsConstructor
public class RechargeOrderService {

    private final RechargeOrderRepository rechargeOrderRepository;
    private final BusinessIdGenerator businessIdGenerator;
    private final WalletService walletService;
    private final PlayerRepository playerRepository;

    @Transactional
    public RechargeOrderDto createOrder(long userId, CreateRechargeOrderRequest request) {
        validatePlayer(userId);
        LocalDateTime now = LocalDateTime.now();
        RechargeOrder order = new RechargeOrder();
        order.setOrderNo(businessIdGenerator.nextOrderNo());
        order.setUserId(userId);
        order.setAmount(request.getAmount());
        order.setGoldAmount(request.getGoldAmount());
        order.setPayChannel(request.getPayChannel());
        order.setStatus("PENDING");
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        rechargeOrderRepository.save(order);
        return toDto(order);
    }

    @Transactional
    public RechargeOrderDto payCallback(String orderNo) {
        RechargeOrder order = rechargeOrderRepository.findById(orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_STATUS);
        }
        LocalDateTime now = LocalDateTime.now();
        order.setStatus("PAID");
        order.setPaidAt(now);
        order.setUpdatedAt(now);
        rechargeOrderRepository.save(order);
        walletService.changeGold(
                order.getUserId(),
                order.getGoldAmount(),
                WalletTransactionType.RECHARGE,
                "RECHARGE_ORDER",
                order.getOrderNo(),
                "充值到账"
        );
        return toDto(order);
    }

    @Transactional(readOnly = true)
    public PageResult<RechargeOrderDto> listOrders(Long userId, String status, PageRequest pageRequest) {
        Specification<RechargeOrder> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<RechargeOrder> page = rechargeOrderRepository.findAll(spec,
                org.springframework.data.domain.PageRequest.of(
                        pageRequest.getPage() - 1,
                        pageRequest.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")));
        List<RechargeOrderDto> list = page.getContent().stream().map(this::toDto).toList();
        return PageResult.of(list, page.getTotalElements(), pageRequest.getPage(), pageRequest.getPageSize());
    }

    private void validatePlayer(long userId) {
        if (!playerRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }
    }

    private RechargeOrderDto toDto(RechargeOrder order) {
        return RechargeOrderDto.builder()
                .orderNo(order.getOrderNo())
                .userId(order.getUserId())
                .amount(order.getAmount())
                .goldAmount(order.getGoldAmount())
                .payChannel(order.getPayChannel())
                .status(order.getStatus())
                .paidAt(order.getPaidAt())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
