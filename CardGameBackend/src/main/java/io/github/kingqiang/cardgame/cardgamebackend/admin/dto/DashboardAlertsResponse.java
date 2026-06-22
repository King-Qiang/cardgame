package io.github.kingqiang.cardgame.cardgamebackend.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * API 响应体：DashboardAlerts。
 */
@Getter
@Builder
public class DashboardAlertsResponse {

    private final List<DashboardAlertItem> alerts;

    @Getter
    @Builder
    public static class DashboardAlertItem {
        private final String level;
        private final String code;
        private final String message;
        private final long count;
        private final String link;
    }
}
