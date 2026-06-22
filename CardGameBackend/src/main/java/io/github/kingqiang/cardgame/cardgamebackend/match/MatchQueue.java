package io.github.kingqiang.cardgame.cardgamebackend.match;

import java.util.List;
import java.util.Optional;

/**
 * MatchQueue。
 */
public interface MatchQueue {

    void enqueue(String queueKey, long userId);

    void cancel(long userId);

    boolean isQueued(long userId);

    String getQueueKey(long userId);

    long getEnqueueTime(long userId);

    int queueSize(String queueKey);

    Optional<List<Long>> pollMatchedPlayers(String queueKey, int requiredPlayers);

    Optional<List<Long>> pollMatchedPlayersFromKeys(List<String> queueKeys, int requiredPlayers);
}
