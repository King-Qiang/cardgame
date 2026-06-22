package io.github.kingqiang.cardgame.cardgamebackend.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

/**
 * API 请求体：Page。
 */
@Getter
@Setter
public class PageRequest {

    @Min(1)
    private int page = 1;

    @Min(1)
    @Max(100)
    private int pageSize = 20;

    public int offset() {
        return (page - 1) * pageSize;
    }
}
