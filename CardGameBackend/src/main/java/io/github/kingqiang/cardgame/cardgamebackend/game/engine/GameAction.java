package io.github.kingqiang.cardgame.cardgamebackend.game.engine;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * GameAction。
 */
@Getter
@Builder
public class GameAction {

    private final String action;
    private final long userId;
    private final int seat;
    private final List<String> cards;
    private final Map<String, Object> extra;
}
