package io.github.kingqiang.cardgame.cardgamebackend.bot;

import io.github.kingqiang.cardgame.cardgamebackend.bot.doudizhu.DoudizhuBot;
import io.github.kingqiang.cardgame.cardgamebackend.bot.doudizhu.EasyDoudizhuBot;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 注册表/工厂：DoudizhuBot。
 */
@Component
public class DoudizhuBotRegistry {

    private final Map<BotDifficulty, DoudizhuBot> bots;

    public DoudizhuBotRegistry(EasyDoudizhuBot easyDoudizhuBot) {
        this.bots = Map.of(
                BotDifficulty.EASY, easyDoudizhuBot,
                BotDifficulty.NORMAL, easyDoudizhuBot,
                BotDifficulty.HARD, easyDoudizhuBot
        );
    }

    public DoudizhuBot get(BotDifficulty difficulty) {
        return bots.getOrDefault(difficulty, bots.get(BotDifficulty.EASY));
    }
}
