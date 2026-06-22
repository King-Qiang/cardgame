package io.github.kingqiang.cardgame.cardgamebackend.admin.controller;

import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.KickRoomPlayerRequest;
import io.github.kingqiang.cardgame.cardgamebackend.admin.service.AdminRoomService;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageRequest;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageResult;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ApiResponse;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.TraceIdHolder;
import io.github.kingqiang.cardgame.cardgamebackend.room.dto.RoomDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 运营后台 REST API 控制器：AdminRoom 相关接口。
 */
@Tag(name = "运营房间监控")
@RestController
@RequestMapping("/api/admin/v1/admin/rooms")
@RequiredArgsConstructor
public class AdminRoomController {

    private final AdminRoomService adminRoomService;

    @Operation(summary = "房间列表")
    @GetMapping
    public ApiResponse<PageResult<AdminRoomService.AdminRoomListItemDto>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String gameType,
            @RequestParam(required = false) String mode,
            PageRequest pageRequest) {
        return ApiResponse.ok(adminRoomService.list(status, gameType, mode, pageRequest))
                .withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "房间详情")
    @GetMapping("/{roomId}")
    public ApiResponse<RoomDetailResponse> detail(@PathVariable String roomId) {
        return ApiResponse.ok(adminRoomService.detail(roomId)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "强制解散房间")
    @PostMapping("/{roomId}/disband")
    public ApiResponse<Void> disband(@PathVariable String roomId, @RequestBody Map<String, String> body) {
        adminRoomService.disband(roomId, body.getOrDefault("reason", ""));
        return ApiResponse.ok().withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "踢出玩家")
    @PostMapping("/{roomId}/kick")
    public ApiResponse<Void> kick(@PathVariable String roomId, @Valid @RequestBody KickRoomPlayerRequest request) {
        adminRoomService.kick(roomId, request.getUserId(), request.getReason());
        return ApiResponse.ok().withTraceId(TraceIdHolder.get());
    }
}
