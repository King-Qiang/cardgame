package io.github.kingqiang.cardgame.cardgamebackend.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * API 响应体：DashboardTrends。
 */
@Getter
@Builder
public class DashboardTrendsResponse {

    private final List<String> dates;
    private final List<Long> games;
    private final List<Long> newUsers;
    private final List<Long> revenue;
}
