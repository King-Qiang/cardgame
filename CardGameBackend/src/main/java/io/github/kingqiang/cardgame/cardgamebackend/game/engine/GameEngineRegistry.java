package io.github.kingqiang.cardgame.cardgamebackend.game.engine;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 注册表/工厂：GameEngine。
 */
@Component
public class GameEngineRegistry {

    private final Map<GameType, GameEngine> engines;

    public GameEngineRegistry(List<GameEngine> engineList) {
        Map<GameType, GameEngine> map = new EnumMap<>(GameType.class);
        for (GameEngine engine : engineList) {
            map.put(engine.type(), engine);
        }
        this.engines = Map.copyOf(map);
    }

    public GameEngine get(GameType type) {
        GameEngine engine = engines.get(type);
        if (engine == null) {
            throw new IllegalArgumentException("Unsupported game type: " + type);
        }
        return engine;
    }

    public GameEngine get(String gameType) {
        return get(GameType.valueOf(gameType));
    }
}
