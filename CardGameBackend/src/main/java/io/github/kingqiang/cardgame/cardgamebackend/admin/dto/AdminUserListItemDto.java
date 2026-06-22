package io.github.kingqiang.cardgame.cardgamebackend.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * API 数据传输对象：AdminUserListItemDto。
 */
@Getter
@Builder
public class AdminUserListItemDto {

    private final Long id;
    private final String nickname;
    private final String avatar;
    private final String openidMasked;
    private final Long gold;
    private final String statusLabel;
    private final String rankTier;
    private final Integer rankPoints;
    private final LocalDateTime createdAt;
}
