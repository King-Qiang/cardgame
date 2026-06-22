package io.github.kingqiang.cardgame.cardgamebackend.game.engine;

import io.github.kingqiang.cardgame.cardgamebackend.game.context.GameContext;

import java.util.List;

/**
 * 游戏引擎实现：Game。
 */
public interface GameEngine {

    GameType type();

    void onPlayerJoin(GameContext ctx, long userId, int seat);

    void onPlayerLeave(GameContext ctx, long userId);

    ActionResult handleAction(GameContext ctx, GameAction action);

    boolean canStart(GameContext ctx);

    void onGameStart(GameContext ctx);

    List<SettlementItem> settle(GameContext ctx);

    Object getVisibleState(GameContext ctx, long userId);
}
