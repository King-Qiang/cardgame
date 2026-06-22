package io.github.kingqiang.cardgame.cardgamebackend.activity.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * API 数据传输对象：DailySignStatusDto。
 */
@Getter
@Builder
public class DailySignStatusDto {

    private final boolean signedToday;
    private final int streakDay;
    private final Long rewardGold;
    private final Long nextRewardGold;
    private final List<Long> rewardPreview;
}
