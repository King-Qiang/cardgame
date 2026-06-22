package io.github.kingqiang.cardgame.cardgamebackend.admin.service;

import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.OperationActions;
import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.service.OperationLogService;
import io.github.kingqiang.cardgame.cardgamebackend.auth.security.SecurityUtils;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageRequest;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageResult;
import io.github.kingqiang.cardgame.cardgamebackend.common.exception.BusinessException;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.RequestUtils;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.AdjustWalletRequest;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.CreateAdjustRequestRequest;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.RejectAdjustRequest;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.WalletAdjustRequestDto;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.WalletChangeResult;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.WalletTransactionDto;
import io.github.kingqiang.cardgame.cardgamebackend.economy.entity.WalletAdjustRequest;
import io.github.kingqiang.cardgame.cardgamebackend.economy.entity.WalletTransaction;
import io.github.kingqiang.cardgame.cardgamebackend.economy.enums.WalletTransactionType;
import io.github.kingqiang.cardgame.cardgamebackend.economy.repository.WalletAdjustRequestRepository;
import io.github.kingqiang.cardgame.cardgamebackend.economy.repository.WalletTransactionRepository;
import io.github.kingqiang.cardgame.cardgamebackend.economy.service.SystemConfigService;
import io.github.kingqiang.cardgame.cardgamebackend.economy.service.WalletService;
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
import java.util.Map;

/**
 * 业务服务：AdminWallet 领域逻辑。
 */
@Service
@RequiredArgsConstructor
public class AdminWalletService {

    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletAdjustRequestRepository walletAdjustRequestRepository;
    private final WalletService walletService;
    private final SystemConfigService systemConfigService;
    private final PlayerRepository playerRepository;
    private final OperationLogService operationLogService;

    @Transactional(readOnly = true)
    public PageResult<WalletTransactionDto> listTransactions(Long userId, String type, PageRequest pageRequest) {
        Specification<WalletTransaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (type != null && !type.isBlank()) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<WalletTransaction> page = walletTransactionRepository.findAll(spec,
                org.springframework.data.domain.PageRequest.of(
                        pageRequest.getPage() - 1,
                        pageRequest.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")));
        List<WalletTransactionDto> list = page.getContent().stream().map(this::toTxDto).toList();
        return PageResult.of(list, page.getTotalElements(), pageRequest.getPage(), pageRequest.getPageSize());
    }

    @Transactional
    public WalletChangeResult adjustWalletDirect(Long userId, AdjustWalletRequest request) {
        validatePlayerExists(userId);
        validateAdjustType(request.getAdjustType());
        if (systemConfigService.isAdjustApprovalRequired(request.getAmount())) {
            throw new BusinessException(ErrorCode.ADJUST_REQUIRES_APPROVAL);
        }
        long delta = toDelta(request.getAdjustType(), request.getAmount());
        long adminId = SecurityUtils.requireAdmin().getId();
        WalletChangeResult result = walletService.changeGold(
                userId, delta, WalletTransactionType.ADMIN_ADJUST,
                "ADMIN", String.valueOf(adminId), request.getReason());
        operationLogService.log(
                adminId,
                OperationActions.WALLET_ADJUST,
                "USER",
                String.valueOf(userId),
                Map.of(
                        "adjustType", request.getAdjustType(),
                        "amount", request.getAmount(),
                        "reason", request.getReason(),
                        "transactionId", result.getTransactionId()
                ),
                RequestUtils.clientIp()
        );
        return result;
    }

    @Transactional
    public WalletAdjustRequestDto createAdjustRequest(CreateAdjustRequestRequest request) {
        validatePlayerExists(request.getUserId());
        validateAdjustType(request.getAdjustType());
        if (!systemConfigService.isAdjustApprovalRequired(request.getAmount())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "金额未达审批阈值，请使用直接调账");
        }
        LocalDateTime now = LocalDateTime.now();
        WalletAdjustRequest entity = new WalletAdjustRequest();
        entity.setUserId(request.getUserId());
        entity.setAdjustType(request.getAdjustType());
        entity.setAmount(request.getAmount());
        entity.setReason(request.getReason());
        entity.setStatus("PENDING");
        entity.setApplicantId(SecurityUtils.requireAdmin().getId());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return toAdjustDto(walletAdjustRequestRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public PageResult<WalletAdjustRequestDto> listAdjustRequests(Long userId, String status, PageRequest pageRequest) {
        Specification<WalletAdjustRequest> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<WalletAdjustRequest> page = walletAdjustRequestRepository.findAll(spec,
                org.springframework.data.domain.PageRequest.of(
                        pageRequest.getPage() - 1,
                        pageRequest.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")));
        List<WalletAdjustRequestDto> list = page.getContent().stream().map(this::toAdjustDto).toList();
        return PageResult.of(list, page.getTotalElements(), pageRequest.getPage(), pageRequest.getPageSize());
    }

    @Transactional
    public WalletAdjustRequestDto approveAdjustRequest(Long id) {
        WalletAdjustRequest entity = findPendingRequest(id);
        long approverId = SecurityUtils.requireAdmin().getId();
        long delta = toDelta(entity.getAdjustType(), entity.getAmount());
        WalletChangeResult result = walletService.changeGold(
                entity.getUserId(), delta, WalletTransactionType.ADMIN_ADJUST,
                "ADJUST_REQUEST", String.valueOf(id), entity.getReason());
        LocalDateTime now = LocalDateTime.now();
        entity.setStatus("EXECUTED");
        entity.setApproverId(approverId);
        entity.setApprovedAt(now);
        entity.setUpdatedAt(now);
        WalletAdjustRequestDto dto = toAdjustDto(walletAdjustRequestRepository.save(entity));
        operationLogService.log(
                approverId,
                OperationActions.WALLET_ADJUST,
                "ADJUST_REQUEST",
                String.valueOf(id),
                Map.of(
                        "userId", entity.getUserId(),
                        "adjustType", entity.getAdjustType(),
                        "amount", entity.getAmount(),
                        "reason", entity.getReason(),
                        "transactionId", result.getTransactionId(),
                        "action", "APPROVE"
                ),
                RequestUtils.clientIp()
        );
        return dto;
    }

    @Transactional
    public WalletAdjustRequestDto rejectAdjustRequest(Long id, RejectAdjustRequest request) {
        WalletAdjustRequest entity = findPendingRequest(id);
        long approverId = SecurityUtils.requireAdmin().getId();
        LocalDateTime now = LocalDateTime.now();
        entity.setStatus("REJECTED");
        entity.setApproverId(approverId);
        entity.setApprovedAt(now);
        entity.setRejectReason(request.getRejectReason());
        entity.setUpdatedAt(now);
        return toAdjustDto(walletAdjustRequestRepository.save(entity));
    }

    private WalletAdjustRequest findPendingRequest(Long id) {
        WalletAdjustRequest entity = walletAdjustRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADJUST_REQUEST_NOT_FOUND));
        if (!"PENDING".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.ADJUST_REQUEST_INVALID_STATUS);
        }
        return entity;
    }

    private void validatePlayerExists(Long userId) {
        if (!playerRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }
    }

    private void validateAdjustType(String adjustType) {
        if (!"INCREASE".equals(adjustType) && !"DECREASE".equals(adjustType)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "adjustType 必须为 INCREASE 或 DECREASE");
        }
    }

    private long toDelta(String adjustType, long amount) {
        return "INCREASE".equals(adjustType) ? amount : -amount;
    }

    private WalletTransactionDto toTxDto(WalletTransaction tx) {
        return WalletTransactionDto.builder()
                .id(tx.getId())
                .userId(tx.getUserId())
                .type(tx.getType())
                .amount(tx.getAmount())
                .balanceAfter(tx.getBalanceAfter())
                .refType(tx.getRefType())
                .refId(tx.getRefId())
                .remark(tx.getRemark())
                .createdAt(tx.getCreatedAt())
                .build();
    }

    private WalletAdjustRequestDto toAdjustDto(WalletAdjustRequest entity) {
        return WalletAdjustRequestDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .adjustType(entity.getAdjustType())
                .amount(entity.getAmount())
                .reason(entity.getReason())
                .status(entity.getStatus())
                .applicantId(entity.getApplicantId())
                .approverId(entity.getApproverId())
                .approvedAt(entity.getApprovedAt())
                .rejectReason(entity.getRejectReason())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
