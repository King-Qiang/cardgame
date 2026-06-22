package io.github.kingqiang.cardgame.cardgamebackend.game.engine;

import lombok.Builder;
import lombok.Getter;

/**
 * ActionResult。
 */
@Getter
@Builder
public class ActionResult {

    private final boolean success;
    private final String message;
    private final boolean gameFinished;

    public static ActionResult ok() {
        return ActionResult.builder().success(true).gameFinished(false).build();
    }

    public static ActionResult finished() {
        return ActionResult.builder().success(true).gameFinished(true).build();
    }

    public static ActionResult fail(String message) {
        return ActionResult.builder().success(false).message(message).gameFinished(false).build();
    }
}
