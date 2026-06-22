package io.github.kingqiang.cardgame.cardgamebackend.activity.service;

import io.github.kingqiang.cardgame.cardgamebackend.activity.dto.DailySignStatusDto;
import io.github.kingqiang.cardgame.cardgamebackend.activity.entity.ActivityConfig;
import io.github.kingqiang.cardgame.cardgamebackend.activity.entity.PlayerSignRecord;
import io.github.kingqiang.cardgame.cardgamebackend.activity.repository.ActivityConfigRepository;
import io.github.kingqiang.cardgame.cardgamebackend.activity.repository.PlayerSignRecordRepository;
import io.github.kingqiang.cardgame.cardgamebackend.common.exception.BusinessException;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import io.github.kingqiang.cardgame.cardgamebackend.economy.enums.WalletTransactionType;
import io.github.kingqiang.cardgame.cardgamebackend.economy.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 业务服务：DailySign 领域逻辑。
 */
@Service
@RequiredArgsConstructor
public class DailySignService {

    private static final String DAILY_SIGN_CODE = "DAILY_SIGN";

    private final ActivityConfigRepository activityConfigRepository;
    private final PlayerSignRecordRepository playerSignRecordRepository;
    private final WalletService walletService;

    @Transactional(readOnly = true)
    public DailySignStatusDto getStatus(long userId) {
        ActivityConfig activity = requireActiveDailySign();
        List<Long> rewards = parseRewards(activity.getConfigJson());
        LocalDate today = LocalDate.now();
        Optional<PlayerSignRecord> todayRecord = playerSignRecordRepository.findByUserIdAndSignDate(userId, today);
        if (todayRecord.isPresent()) {
            PlayerSignRecord record = todayRecord.get();
            int nextStreak = record.getStreakDay() >= rewards.size() ? 1 : record.getStreakDay() + 1;
            return DailySignStatusDto.builder()
                    .signedToday(true)
                    .streakDay(record.getStreakDay())
                    .rewardGold(record.getRewardGold())
                    .nextRewardGold(rewardForDay(rewards, nextStreak))
                    .rewardPreview(rewards)
                    .build();
        }
        int nextStreakDay = calcNextStreakDay(userId, today, rewards);
        int nextAfterSign = nextStreakDay >= rewards.size() ? 1 : nextStreakDay + 1;
        return DailySignStatusDto.builder()
                .signedToday(false)
                .streakDay(0)
                .rewardGold(rewardForDay(rewards, nextStreakDay))
                .nextRewardGold(rewardForDay(rewards, nextAfterSign))
                .rewardPreview(rewards)
                .build();
    }

    @Transactional
    public DailySignStatusDto sign(long userId) {
        ActivityConfig activity = requireActiveDailySign();
        List<Long> rewards = parseRewards(activity.getConfigJson());
        LocalDate today = LocalDate.now();
        if (playerSignRecordRepository.findByUserIdAndSignDate(userId, today).isPresent()) {
            throw new BusinessException(ErrorCode.ALREADY_SIGNED_TODAY);
        }
        int streakDay = calcNextStreakDay(userId, today, rewards);
        long rewardGold = rewardForDay(rewards, streakDay);
        LocalDateTime now = LocalDateTime.now();
        PlayerSignRecord record = new PlayerSignRecord();
        record.setUserId(userId);
        record.setSignDate(today);
        record.setStreakDay(streakDay);
        record.setRewardGold(rewardGold);
        record.setCreatedAt(now);
        playerSignRecordRepository.save(record);
        walletService.changeGold(
                userId,
                rewardGold,
                WalletTransactionType.DAILY_REWARD,
                "DAILY_SIGN",
                String.valueOf(record.getId()),
                "每日签到奖励"
        );
        int nextStreak = streakDay >= rewards.size() ? 1 : streakDay + 1;
        return DailySignStatusDto.builder()
                .signedToday(true)
                .streakDay(streakDay)
                .rewardGold(rewardGold)
                .nextRewardGold(rewardForDay(rewards, nextStreak))
                .rewardPreview(rewards)
                .build();
    }

    private ActivityConfig requireActiveDailySign() {
        ActivityConfig activity = activityConfigRepository.findByCode(DAILY_SIGN_CODE)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND));
        if (activity.getStatus() == null || activity.getStatus() != 1) {
            throw new BusinessException(ErrorCode.ACTIVITY_DISABLED);
        }
        return activity;
    }

    private int calcNextStreakDay(long userId, LocalDate today, List<Long> rewards) {
        return playerSignRecordRepository.findTopByUserIdOrderBySignDateDesc(userId)
                .map(last -> {
                    if (last.getSignDate().equals(today.minusDays(1))) {
                        int next = last.getStreakDay() + 1;
                        return next > rewards.size() ? 1 : next;
                    }
                    return 1;
                })
                .orElse(1);
    }

    @SuppressWarnings("unchecked")
    private List<Long> parseRewards(Object configJson) {
        if (configJson instanceof Map<?, ?> map) {
            Object rewards = map.get("rewards");
            if (rewards instanceof List<?> list) {
                List<Long> result = new ArrayList<>();
                for (Object item : list) {
                    if (item instanceof Number number) {
                        result.add(number.longValue());
                    }
                }
                if (!result.isEmpty()) {
                    return result;
                }
            }
        }
        return List.of(100L, 100L, 200L, 200L, 300L, 300L, 500L);
    }

    private long rewardForDay(List<Long> rewards, int streakDay) {
        if (rewards.isEmpty()) {
            return 0L;
        }
        int index = Math.max(1, Math.min(streakDay, rewards.size())) - 1;
        return rewards.get(index);
    }
}
