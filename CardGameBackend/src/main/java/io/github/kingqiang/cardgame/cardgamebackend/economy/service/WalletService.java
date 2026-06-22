package io.github.kingqiang.cardgame.cardgamebackend.economy.service;

import io.github.kingqiang.cardgame.cardgamebackend.common.exception.BusinessException;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.WalletChangeResult;
import io.github.kingqiang.cardgame.cardgamebackend.economy.entity.WalletTransaction;
import io.github.kingqiang.cardgame.cardgamebackend.economy.enums.WalletTransactionType;
import io.github.kingqiang.cardgame.cardgamebackend.economy.repository.WalletTransactionRepository;
import io.github.kingqiang.cardgame.cardgamebackend.user.entity.UserWallet;
import io.github.kingqiang.cardgame.cardgamebackend.user.repository.UserWalletRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 业务服务：Wallet 领域逻辑。
 */
@Service
@RequiredArgsConstructor
public class WalletService {

    private static final int MAX_RETRIES = 3;

    private final UserWalletRepository userWalletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    @Transactional
    public WalletChangeResult changeGold(long userId, long amount, WalletTransactionType type,
                                         String refType, String refId, String remark) {
        if (amount == 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "变动金额不能为 0");
        }
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                return doChangeGold(userId, amount, type, refType, refId, remark);
            } catch (OptimisticLockException | ObjectOptimisticLockingFailureException ex) {
                if (attempt == MAX_RETRIES - 1) {
                    throw new BusinessException(ErrorCode.WALLET_UPDATE_CONFLICT);
                }
            }
        }
        throw new BusinessException(ErrorCode.WALLET_UPDATE_CONFLICT);
    }

    private WalletChangeResult doChangeGold(long userId, long amount, WalletTransactionType type,
                                            String refType, String refId, String remark) {
        UserWallet wallet = userWalletRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYER_NOT_FOUND, "钱包不存在"));

        long newBalance = wallet.getGold() + amount;
        if (newBalance < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        LocalDateTime now = LocalDateTime.now();
        wallet.setGold(newBalance);
        wallet.setUpdatedAt(now);
        userWalletRepository.save(wallet);

        WalletTransaction tx = new WalletTransaction();
        tx.setUserId(userId);
        tx.setType(type.name());
        tx.setAmount(amount);
        tx.setBalanceAfter(newBalance);
        tx.setRefType(refType != null ? refType : "");
        tx.setRefId(refId != null ? refId : "");
        tx.setRemark(remark != null ? remark : "");
        tx.setCreatedAt(now);
        tx = walletTransactionRepository.save(tx);

        return WalletChangeResult.builder()
                .transactionId(tx.getId())
                .userId(userId)
                .amount(amount)
                .balanceAfter(newBalance)
                .build();
    }

    @Transactional(readOnly = true)
    public long getGoldBalance(long userId) {
        return userWalletRepository.findById(userId)
                .map(UserWallet::getGold)
                .orElse(0L);
    }
}
