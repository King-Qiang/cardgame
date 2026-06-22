package io.github.kingqiang.cardgame.cardgamebackend.admin.controller;

import io.github.kingqiang.cardgame.cardgamebackend.activity.dto.ActivityDto;
import io.github.kingqiang.cardgame.cardgamebackend.activity.dto.ActivityUpsertRequest;
import io.github.kingqiang.cardgame.cardgamebackend.admin.service.AdminActivityService;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageRequest;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageResult;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ApiResponse;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.TraceIdHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运营后台 REST API 控制器：AdminActivity 相关接口。
 */
@Tag(name = "活动管理")
@RestController
@RequestMapping("/api/admin/v1/admin/activities")
@RequiredArgsConstructor
public class AdminActivityController {

    private final AdminActivityService adminActivityService;

    @Operation(summary = "活动列表")
    @GetMapping
    public ApiResponse<PageResult<ActivityDto>> list(@RequestParam(required = false) Integer status,
                                                     @RequestParam(required = false) String type,
                                                     PageRequest pageRequest) {
        return ApiResponse.ok(adminActivityService.list(status, type, pageRequest)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "活动详情")
    @GetMapping("/{id}")
    public ApiResponse<ActivityDto> get(@PathVariable Long id) {
        return ApiResponse.ok(adminActivityService.get(id)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "新增活动")
    @PostMapping
    public ApiResponse<ActivityDto> create(@Valid @RequestBody ActivityUpsertRequest request) {
        return ApiResponse.ok(adminActivityService.create(request)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "更新活动")
    @PutMapping("/{id}")
    public ApiResponse<ActivityDto> update(@PathVariable Long id, @Valid @RequestBody ActivityUpsertRequest request) {
        return ApiResponse.ok(adminActivityService.update(id, request)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "删除活动")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminActivityService.delete(id);
        return ApiResponse.ok().withTraceId(TraceIdHolder.get());
    }
}
