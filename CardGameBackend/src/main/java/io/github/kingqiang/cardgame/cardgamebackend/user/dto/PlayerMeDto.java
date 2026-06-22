package io.github.kingqiang.cardgame.cardgamebackend.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * API 数据传输对象：PlayerMeDto。
 */
@Getter
@Builder
public class PlayerMeDto {

    private final Long id;
    private final String nickname;
    private final String avatar;
    private final long gold;
    private final LocalDateTime createdAt;
    private final RankSummaryDto rankSummary;
}
