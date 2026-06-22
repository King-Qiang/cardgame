package io.github.kingqiang.cardgame.cardgamebackend.user;

/**
 * 系统虚拟玩家常量；900000 用于 game_action_log 系统事件 FK。
 */
public final class SystemPlayers {

    /** 对局系统事件（发牌、开局、结算日志）使用的虚拟玩家 ID */
    public static final long SYSTEM_ACTOR_ID = 900_000L;

    private SystemPlayers() {
    }
}
