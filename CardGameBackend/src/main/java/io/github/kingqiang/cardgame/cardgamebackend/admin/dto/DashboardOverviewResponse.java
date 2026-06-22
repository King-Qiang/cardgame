package io.github.kingqiang.cardgame.cardgamebackend.admin.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * API 响应体：DashboardOverview。
 */
@Getter
@Builder
public class DashboardOverviewResponse {

    private final long todayDau;
    private final long onlineCount;
    private final long todayGames;
    private final long todayRechargeAmount;
}
