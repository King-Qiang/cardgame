package io.github.kingqiang.cardgame.cardgamebackend.economy.service;

import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageRequest;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageResult;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.WalletBalanceDto;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.WalletTransactionDto;
import io.github.kingqiang.cardgame.cardgamebackend.economy.entity.WalletTransaction;
import io.github.kingqiang.cardgame.cardgamebackend.economy.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 业务服务：PlayerWallet 领域逻辑。
 */
@Service
@RequiredArgsConstructor
public class PlayerWalletService {

    private final WalletService walletService;
    private final WalletTransactionRepository walletTransactionRepository;

    @Transactional(readOnly = true)
    public WalletBalanceDto getBalance(long userId) {
        return WalletBalanceDto.builder()
                .userId(userId)
                .gold(walletService.getGoldBalance(userId))
                .build();
    }

    @Transactional(readOnly = true)
    public PageResult<WalletTransactionDto> listTransactions(long userId, PageRequest pageRequest) {
        Specification<WalletTransaction> spec = (root, query, cb) ->
                cb.equal(root.get("userId"), userId);
        Page<WalletTransaction> page = walletTransactionRepository.findAll(spec,
                org.springframework.data.domain.PageRequest.of(
                        pageRequest.getPage() - 1,
                        pageRequest.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")));
        List<WalletTransactionDto> list = page.getContent().stream().map(this::toDto).toList();
        return PageResult.of(list, page.getTotalElements(), pageRequest.getPage(), pageRequest.getPageSize());
    }

    private WalletTransactionDto toDto(WalletTransaction tx) {
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
}
