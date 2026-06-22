package io.github.kingqiang.cardgame.cardgamebackend.admin.service;

import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.DashboardAlertsResponse;
import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.DashboardOverviewResponse;
import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.DashboardTrendsResponse;
import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.OperationActions;
import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.repository.OperationLogRepository;
import io.github.kingqiang.cardgame.cardgamebackend.economy.repository.RechargeOrderRepository;
import io.github.kingqiang.cardgame.cardgamebackend.economy.repository.WalletAdjustRequestRepository;
import io.github.kingqiang.cardgame.cardgamebackend.record.repository.GameRecordRepository;
import io.github.kingqiang.cardgame.cardgamebackend.room.websocket.WebSocketSessionManager;
import io.github.kingqiang.cardgame.cardgamebackend.user.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 业务服务：AdminDashboard 领域逻辑。
 */
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final PlayerRepository playerRepository;
    private final GameRecordRepository gameRecordRepository;
    private final RechargeOrderRepository rechargeOrderRepository;
    private final WebSocketSessionManager webSocketSessionManager;
    private final WalletAdjustRequestRepository walletAdjustRequestRepository;
    private final OperationLogRepository operationLogRepository;

    public DashboardOverviewResponse overview() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        long totalPlayers = playerRepository.count();
        long todayGames = gameRecordRepository.count((root, query, cb) ->
                cb.between(root.get("startAt"), startOfDay, endOfDay));
        long todayRecharge = sumPaidRecharge(startOfDay, endOfDay);
        return DashboardOverviewResponse.builder()
                .todayDau(totalPlayers)
                .onlineCount(webSocketSessionManager.onlinePlayerCount())
                .todayGames(todayGames)
                .todayRechargeAmount(todayRecharge)
                .build();
    }

    public DashboardTrendsResponse trends(int days) {
        int range = Math.min(Math.max(days, 1), 30);
        List<String> dates = new ArrayList<>();
        List<Long> games = new ArrayList<>();
        List<Long> newUsers = new ArrayList<>();
        List<Long> revenue = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = range - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            dates.add(date.toString());
            games.add(gameRecordRepository.count((root, query, cb) ->
                    cb.between(root.get("startAt"), start, end)));
            newUsers.add(playerRepository.count((root, query, cb) ->
                    cb.between(root.get("createdAt"), start, end)));
            revenue.add(sumPaidRecharge(start, end));
        }
        return DashboardTrendsResponse.builder()
                .dates(dates)
                .games(games)
                .newUsers(newUsers)
                .revenue(revenue)
                .build();
    }

    public DashboardAlertsResponse alerts() {
        List<DashboardAlertsResponse.DashboardAlertItem> items = new ArrayList<>();
        long pendingAdjust = walletAdjustRequestRepository.count((root, query, cb) ->
                cb.equal(root.get("status"), "PENDING"));
        if (pendingAdjust > 0) {
            items.add(DashboardAlertsResponse.DashboardAlertItem.builder()
                    .level("WARNING")
                    .code("ADJUST_PENDING")
                    .message(pendingAdjust + " 笔大额调账待审批")
                    .count(pendingAdjust)
                    .link("/economy/adjust-requests?status=PENDING")
                    .build());
        }

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        long todayBans = operationLogRepository.count((root, query, cb) -> cb.and(
                cb.equal(root.get("action"), OperationActions.USER_BAN),
                cb.between(root.get("createdAt"), startOfDay, endOfDay)
        ));
        double avgBans = averageDailyCount(OperationActions.USER_BAN, 7);
        if (todayBans >= 5 && todayBans > avgBans * 2) {
            items.add(DashboardAlertsResponse.DashboardAlertItem.builder()
                    .level("INFO")
                    .code("BAN_SPIKE")
                    .message("今日封禁 " + todayBans + " 人，高于 7 日均值")
                    .count(todayBans)
                    .link("/users")
                    .build());
        }

        long todayDisband = operationLogRepository.count((root, query, cb) -> cb.and(
                cb.equal(root.get("action"), OperationActions.ROOM_DISBAND),
                cb.between(root.get("createdAt"), startOfDay, endOfDay)
        ));
        if (todayDisband > 3) {
            items.add(DashboardAlertsResponse.DashboardAlertItem.builder()
                    .level("WARNING")
                    .code("ROOM_DISBAND_SPIKE")
                    .message("今日强制解散 " + todayDisband + " 间，请留意异常")
                    .count(todayDisband)
                    .link("/rooms?status=DISBANDED")
                    .build());
        }

        return DashboardAlertsResponse.builder().alerts(items).build();
    }

    private double averageDailyCount(String action, int days) {
        LocalDate today = LocalDate.now();
        long total = 0;
        for (int i = 0; i < days; i++) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            total += operationLogRepository.count((root, query, cb) -> cb.and(
                    cb.equal(root.get("action"), action),
                    cb.between(root.get("createdAt"), start, end)
            ));
        }
        return days == 0 ? 0 : (double) total / days;
    }

    private long sumPaidRecharge(LocalDateTime start, LocalDateTime end) {
        return rechargeOrderRepository.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("status"), "PAID"),
                cb.between(root.get("paidAt"), start, end)
        )).stream().mapToLong(order -> order.getAmount() != null ? order.getAmount() : 0L).sum();
    }
}
