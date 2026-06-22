package io.github.kingqiang.cardgame.cardgamebackend.game.engine;

import lombok.Builder;
import lombok.Getter;

/**
 * SettlementItem。
 */
@Getter
@Builder
public class SettlementItem {

    private final long userId;
    private final int seat;
    private final long scoreDelta;
    private final long goldDelta;
    private final int multiplier;
}
