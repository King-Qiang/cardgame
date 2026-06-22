package io.github.kingqiang.cardgame.cardgamebackend.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * PageResult。
 */
@Getter
@AllArgsConstructor
public class PageResult<T> {

    private final List<T> list;
    private final long total;
    private final int page;
    private final int pageSize;

    public static <T> PageResult<T> of(List<T> list, long total, int page, int pageSize) {
        return new PageResult<>(list, total, page, pageSize);
    }
}
