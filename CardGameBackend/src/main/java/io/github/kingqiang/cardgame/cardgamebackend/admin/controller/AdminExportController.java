package io.github.kingqiang.cardgame.cardgamebackend.admin.controller;

import io.github.kingqiang.cardgame.cardgamebackend.admin.service.AdminExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 运营后台 REST API 控制器：AdminExport 相关接口。
 */
@Tag(name = "运营数据导出")
@RestController
@RequestMapping("/api/admin/v1/admin/export")
@RequiredArgsConstructor
public class AdminExportController {

    private final AdminExportService adminExportService;

    @Operation(summary = "导出金币流水 CSV")
    @GetMapping("/wallet/transactions")
    public void exportTransactions(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "csv") String format,
            HttpServletResponse response) throws IOException {
        adminExportService.exportWalletTransactions(userId, type, response);
    }

    @Operation(summary = "导出充值订单 CSV")
    @GetMapping("/orders")
    public void exportOrders(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "csv") String format,
            HttpServletResponse response) throws IOException {
        adminExportService.exportOrders(userId, status, response);
    }

    @Operation(summary = "导出对局记录 CSV")
    @GetMapping("/records")
    public void exportRecords(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String gameType,
            @RequestParam(defaultValue = "csv") String format,
            HttpServletResponse response) throws IOException {
        adminExportService.exportRecords(status, gameType, response);
    }

    @Operation(summary = "导出操作日志 CSV")
    @GetMapping("/operation-logs")
    public void exportOperationLogs(
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetType,
            @RequestParam(defaultValue = "csv") String format,
            HttpServletResponse response) throws IOException {
        adminExportService.exportOperationLogs(operatorId, action, targetType, response);
    }
}
