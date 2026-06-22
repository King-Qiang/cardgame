package io.github.kingqiang.cardgame.cardgamebackend.room.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * API 请求体：Ready。
 */
@Getter
@Setter
public class ReadyRequest {

    private boolean ready = true;
}
