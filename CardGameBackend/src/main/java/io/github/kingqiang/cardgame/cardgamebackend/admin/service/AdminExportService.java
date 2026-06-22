package io.github.kingqiang.cardgame.cardgamebackend.admin.service;

import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.OperationActions;
import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.entity.OperationLog;
import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.repository.OperationLogRepository;
import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.service.OperationLogService;
import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.DashboardAlertsResponse;
import io.github.kingqiang.cardgame.cardgamebackend.auth.security.SecurityUtils;
import io.github.kingqiang.cardgame.cardgamebackend.common.exception.BusinessException;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.CsvUtils;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.RequestUtils;
import io.github.kingqiang.cardgame.cardgamebackend.economy.entity.RechargeOrder;
import io.github.kingqiang.cardgame.cardgamebackend.economy.entity.WalletAdjustRequest;
import io.github.kingqiang.cardgame.cardgamebackend.economy.entity.WalletTransaction;
import io.github.kingqiang.cardgame.cardgamebackend.economy.repository.RechargeOrderRepository;
import io.github.kingqiang.cardgame.cardgamebackend.economy.repository.WalletAdjustRequestRepository;
import io.github.kingqiang.cardgame.cardgamebackend.economy.repository.WalletTransactionRepository;
import io.github.kingqiang.cardgame.cardgamebackend.record.entity.GameRecord;
import io.github.kingqiang.cardgame.cardgamebackend.record.repository.GameRecordRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 业务服务：AdminExport 领域逻辑。
 */
@Service
@RequiredArgsConstructor
public class AdminExportService {

    private static final int MAX_TRANSACTIONS = 50_000;
    private static final int MAX_ORDERS = 50_000;
    private static final int MAX_RECORDS = 20_000;
    private static final int MAX_OPERATION_LOGS = 20_000;
    private static final int BATCH_SIZE = 1000;
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final WalletTransactionRepository walletTransactionRepository;
    private final RechargeOrderRepository rechargeOrderRepository;
    private final GameRecordRepository gameRecordRepository;
    private final OperationLogRepository operationLogRepository;
    private final OperationLogService operationLogService;

    @Transactional(readOnly = true)
    public void exportWalletTransactions(Long userId, String type, HttpServletResponse response) throws IOException {
        Specification<WalletTransaction> spec = buildTxSpec(userId, type);
        long total = walletTransactionRepository.count(spec);
        ensureWithinLimit(total, MAX_TRANSACTIONS);
        auditExport("WALLET_TRANSACTION", exportDetail("userId", userId, "type", type, "rows", total));
        writeCsv(response, "wallet_transactions.csv", new String[]{"id", "userId", "type", "amount", "balanceAfter", "remark", "createdAt"},
                writer -> writeTransactions(writer, spec, total));
    }

    @Transactional(readOnly = true)
    public void exportOrders(Long userId, String status, HttpServletResponse response) throws IOException {
        Specification<RechargeOrder> spec = buildOrderSpec(userId, status);
        long total = rechargeOrderRepository.count(spec);
        ensureWithinLimit(total, MAX_ORDERS);
        auditExport("RECHARGE_ORDER", exportDetail("userId", userId, "status", status, "rows", total));
        writeCsv(response, "recharge_orders.csv", new String[]{"orderNo", "userId", "amount", "goldAmount", "payChannel", "status", "paidAt", "createdAt"},
                writer -> writeOrders(writer, spec, total));
    }

    @Transactional(readOnly = true)
    public void exportRecords(String status, String gameType, HttpServletResponse response) throws IOException {
        Specification<GameRecord> spec = buildRecordSpec(status, gameType);
        long total = gameRecordRepository.count(spec);
        ensureWithinLimit(total, MAX_RECORDS);
        auditExport("GAME_RECORD", exportDetail("status", status, "gameType", gameType, "rows", total));
        writeCsv(response, "game_records.csv", new String[]{"recordId", "roomId", "gameType", "status", "startAt", "endAt"},
                writer -> writeRecords(writer, spec, total));
    }

    @Transactional(readOnly = true)
    public void exportOperationLogs(Long operatorId, String action, String targetType, HttpServletResponse response) throws IOException {
        Specification<OperationLog> spec = buildLogSpec(operatorId, action, targetType);
        long total = operationLogRepository.count(spec);
        ensureWithinLimit(total, MAX_OPERATION_LOGS);
        auditExport("OPERATION_LOG", exportDetail("operatorId", operatorId, "action", action, "targetType", targetType, "rows", total));
        writeCsv(response, "operation_logs.csv", new String[]{"id", "operatorId", "action", "targetType", "targetId", "ip", "createdAt"},
                writer -> writeLogs(writer, spec, total));
    }

    private void writeTransactions(PrintWriter writer, Specification<WalletTransaction> spec, long total) {
        int pages = (int) Math.ceil((double) total / BATCH_SIZE);
        for (int page = 0; page < pages; page++) {
            Page<WalletTransaction> batch = walletTransactionRepository.findAll(spec,
                    PageRequest.of(page, BATCH_SIZE, Sort.by(Sort.Direction.DESC, "createdAt")));
            for (WalletTransaction tx : batch.getContent()) {
                writer.println(CsvUtils.row(
                        String.valueOf(tx.getId()),
                        String.valueOf(tx.getUserId()),
                        tx.getType(),
                        String.valueOf(tx.getAmount()),
                        String.valueOf(tx.getBalanceAfter()),
                        tx.getRemark(),
                        formatDateTime(tx.getCreatedAt())
                ));
            }
        }
    }

    private void writeOrders(PrintWriter writer, Specification<RechargeOrder> spec, long total) {
        int pages = (int) Math.ceil((double) total / BATCH_SIZE);
        for (int page = 0; page < pages; page++) {
            Page<RechargeOrder> batch = rechargeOrderRepository.findAll(spec,
                    PageRequest.of(page, BATCH_SIZE, Sort.by(Sort.Direction.DESC, "createdAt")));
            for (RechargeOrder order : batch.getContent()) {
                writer.println(CsvUtils.row(
                        order.getOrderNo(),
                        String.valueOf(order.getUserId()),
                        String.valueOf(order.getAmount()),
                        String.valueOf(order.getGoldAmount()),
                        order.getPayChannel(),
                        order.getStatus(),
                        formatDateTime(order.getPaidAt()),
                        formatDateTime(order.getCreatedAt())
                ));
            }
        }
    }

    private void writeRecords(PrintWriter writer, Specification<GameRecord> spec, long total) {
        int pages = (int) Math.ceil((double) total / BATCH_SIZE);
        for (int page = 0; page < pages; page++) {
            Page<GameRecord> batch = gameRecordRepository.findAll(spec,
                    PageRequest.of(page, BATCH_SIZE, Sort.by(Sort.Direction.DESC, "startAt")));
            for (GameRecord record : batch.getContent()) {
                writer.println(CsvUtils.row(
                        record.getRecordId(),
                        record.getRoomId(),
                        record.getGameType(),
                        record.getStatus(),
                        formatDateTime(record.getStartAt()),
                        formatDateTime(record.getEndAt())
                ));
            }
        }
    }

    private void writeLogs(PrintWriter writer, Specification<OperationLog> spec, long total) {
        int pages = (int) Math.ceil((double) total / BATCH_SIZE);
        for (int page = 0; page < pages; page++) {
            Page<OperationLog> batch = operationLogRepository.findAll(spec,
                    PageRequest.of(page, BATCH_SIZE, Sort.by(Sort.Direction.DESC, "createdAt")));
            for (OperationLog log : batch.getContent()) {
                writer.println(CsvUtils.row(
                        String.valueOf(log.getId()),
                        String.valueOf(log.getOperatorId()),
                        log.getAction(),
                        log.getTargetType(),
                        log.getTargetId(),
                        log.getIp(),
                        formatDateTime(log.getCreatedAt())
                ));
            }
        }
    }

    private Specification<WalletTransaction> buildTxSpec(Long userId, String type) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (type != null && !type.isBlank()) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<RechargeOrder> buildOrderSpec(Long userId, String status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<GameRecord> buildRecordSpec(String status, String gameType) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (gameType != null && !gameType.isBlank()) {
                predicates.add(cb.equal(root.get("gameType"), gameType));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<OperationLog> buildLogSpec(Long operatorId, String action, String targetType) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (operatorId != null) {
                predicates.add(cb.equal(root.get("operatorId"), operatorId));
            }
            if (action != null && !action.isBlank()) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (targetType != null && !targetType.isBlank()) {
                predicates.add(cb.equal(root.get("targetType"), targetType));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void ensureWithinLimit(long total, int max) {
        if (total > max) {
            throw new BusinessException(ErrorCode.EXPORT_TOO_LARGE,
                    "导出条数 " + total + " 超过上限 " + max + "，请缩小筛选范围");
        }
    }

    private void writeCsv(HttpServletResponse response, String filename, String[] headers, CsvWriter writerLogic) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8));
        writer.write('\uFEFF');
        writer.println(CsvUtils.row(headers));
        writerLogic.write(writer);
        writer.flush();
    }

    private void auditExport(String targetType, Map<String, Object> detail) {
        operationLogService.log(
                SecurityUtils.requireAdmin().getId(),
                OperationActions.EXPORT_DATA,
                targetType,
                "CSV",
                detail,
                RequestUtils.clientIp()
        );
    }

    /** Map.of 不允许 null；筛选参数未填时为 null，需跳过。 */
    private Map<String, Object> exportDetail(Object... keyValues) {
        Map<String, Object> detail = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            Object value = keyValues[i + 1];
            if (value != null) {
                detail.put(String.valueOf(keyValues[i]), value);
            }
        }
        return detail;
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : value.format(DT);
    }

    @FunctionalInterface
    private interface CsvWriter {
        void write(PrintWriter writer);
    }
}
