package io.github.kingqiang.cardgame.cardgamebackend.room.controller;

import io.github.kingqiang.cardgame.cardgamebackend.auth.security.SecurityUtils;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ApiResponse;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.TraceIdHolder;
import io.github.kingqiang.cardgame.cardgamebackend.room.dto.AddRoomBotRequest;
import io.github.kingqiang.cardgame.cardgamebackend.room.dto.CreatePveRoomRequest;
import io.github.kingqiang.cardgame.cardgamebackend.room.dto.CreateRoomRequest;
import io.github.kingqiang.cardgame.cardgamebackend.room.dto.ReadyRequest;
import io.github.kingqiang.cardgame.cardgamebackend.room.dto.RoomDetailResponse;
import io.github.kingqiang.cardgame.cardgamebackend.room.service.PveRoomService;
import io.github.kingqiang.cardgame.cardgamebackend.room.service.RoomBotService;
import io.github.kingqiang.cardgame.cardgamebackend.room.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 玩家 REST API 控制器：Room 相关接口。
 */
@Tag(name = "玩家房间")
@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final PveRoomService pveRoomService;
    private final RoomBotService roomBotService;

    @Operation(summary = "创建房间")
    @PostMapping
    public ApiResponse<RoomDetailResponse> create(@Valid @RequestBody CreateRoomRequest request) {
        long userId = SecurityUtils.requirePlayerId();
        return ApiResponse.ok(roomService.createRoom(userId, request)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "房间详情")
    @GetMapping("/{roomId}")
    public ApiResponse<RoomDetailResponse> detail(@PathVariable String roomId) {
        return ApiResponse.ok(roomService.getRoom(roomId)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "加入房间")
    @PostMapping("/{roomId}/join")
    public ApiResponse<RoomDetailResponse> join(@PathVariable String roomId) {
        long userId = SecurityUtils.requirePlayerId();
        return ApiResponse.ok(roomService.joinRoom(roomId, userId)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "离开房间")
    @PostMapping("/{roomId}/leave")
    public ApiResponse<RoomDetailResponse> leave(@PathVariable String roomId) {
        long userId = SecurityUtils.requirePlayerId();
        return ApiResponse.ok(roomService.leaveRoom(roomId, userId)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "准备/取消准备")
    @PostMapping("/{roomId}/ready")
    public ApiResponse<RoomDetailResponse> ready(@PathVariable String roomId, @RequestBody ReadyRequest request) {
        long userId = SecurityUtils.requirePlayerId();
        return ApiResponse.ok(roomService.setReady(roomId, userId, request.isReady())).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "房主开始游戏")
    @PostMapping("/{roomId}/start")
    public ApiResponse<RoomDetailResponse> start(@PathVariable String roomId) {
        long userId = SecurityUtils.requirePlayerId();
        return ApiResponse.ok(roomService.startRoom(roomId, userId)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "一键人机练习")
    @PostMapping("/pve")
    public ApiResponse<RoomDetailResponse> createPve(@Valid @RequestBody CreatePveRoomRequest request) {
        long userId = SecurityUtils.requirePlayerId();
        return ApiResponse.ok(pveRoomService.createPveRoom(userId, request)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "亲友房添加电脑")
    @PostMapping("/{roomId}/bots")
    public ApiResponse<RoomDetailResponse> addBots(@PathVariable String roomId,
                                                   @RequestBody(required = false) AddRoomBotRequest request) {
        long userId = SecurityUtils.requirePlayerId();
        return ApiResponse.ok(roomBotService.addBots(roomId, userId, request)).withTraceId(TraceIdHolder.get());
    }

    @Operation(summary = "亲友房移除电脑")
    @DeleteMapping("/{roomId}/bots/{seat}")
    public ApiResponse<RoomDetailResponse> removeBot(@PathVariable String roomId, @PathVariable int seat) {
        long userId = SecurityUtils.requirePlayerId();
        return ApiResponse.ok(roomBotService.removeBot(roomId, userId, seat)).withTraceId(TraceIdHolder.get());
    }
}
