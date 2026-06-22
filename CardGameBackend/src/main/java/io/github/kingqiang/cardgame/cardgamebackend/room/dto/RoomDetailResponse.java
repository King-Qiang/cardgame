package io.github.kingqiang.cardgame.cardgamebackend.room.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * API 响应体：RoomDetail。
 */
@Getter
@Builder
public class RoomDetailResponse {

    private final String roomId;
    private final String gameType;
    private final String mode;
    private final String status;
    private final Long ownerId;
    private final int maxPlayers;
    private final Map<String, Object> config;
    private final List<RoomPlayerDto> players;
    private final LocalDateTime createdAt;

    @Getter
    @Builder
    public static class RoomPlayerDto {
        private final Long userId;
        private final String nickname;
        private final String avatar;
        private final int seat;
        private final boolean ready;
        private final boolean isRobot;
    }
}
