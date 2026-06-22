package io.github.kingqiang.cardgame.cardgamebackend.bot.doudizhu;

import io.github.kingqiang.cardgame.cardgamebackend.bot.BotDifficulty;
import io.github.kingqiang.cardgame.cardgamebackend.game.context.GameContext;
import io.github.kingqiang.cardgame.cardgamebackend.game.engine.GameAction;

/**
 * DoudizhuBot。
 */
public interface DoudizhuBot {

    GameAction decide(GameContext ctx, int seat, BotDifficulty difficulty);
}
