package io.github.kingqiang.cardgame.cardgamebackend.match;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * InMemoryMatchQueue。
 */
@Component
@ConditionalOnProperty(prefix = "cardgame.redis", name = "enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryMatchQueue implements MatchQueue {

    private final Map<String, ConcurrentLinkedQueue<Long>> queues = new ConcurrentHashMap<>();
    private final Map<Long, String> userQueueKey = new ConcurrentHashMap<>();
    private final Map<Long, Long> userEnqueueTime = new ConcurrentHashMap<>();

    @Override
    public void enqueue(String queueKey, long userId) {
        queues.computeIfAbsent(queueKey, k -> new ConcurrentLinkedQueue<>()).add(userId);
        userQueueKey.put(userId, queueKey);
        userEnqueueTime.put(userId, System.currentTimeMillis());
    }

    @Override
    public void cancel(long userId) {
        String queueKey = userQueueKey.remove(userId);
        userEnqueueTime.remove(userId);
        if (queueKey != null) {
            ConcurrentLinkedQueue<Long> queue = queues.get(queueKey);
            if (queue != null) {
                queue.remove(userId);
            }
        }
    }

    @Override
    public boolean isQueued(long userId) {
        return userQueueKey.containsKey(userId);
    }

    @Override
    public String getQueueKey(long userId) {
        return userQueueKey.get(userId);
    }

    @Override
    public long getEnqueueTime(long userId) {
        return userEnqueueTime.getOrDefault(userId, System.currentTimeMillis());
    }

    @Override
    public int queueSize(String queueKey) {
        ConcurrentLinkedQueue<Long> queue = queues.get(queueKey);
        return queue == null ? 0 : queue.size();
    }

    @Override
    public Optional<List<Long>> pollMatchedPlayers(String queueKey, int requiredPlayers) {
        return pollMatchedPlayersFromKeys(List.of(queueKey), requiredPlayers);
    }

    @Override
    public Optional<List<Long>> pollMatchedPlayersFromKeys(List<String> queueKeys, int requiredPlayers) {
        List<QueuedEntry> candidates = new ArrayList<>();
        for (String queueKey : queueKeys) {
            ConcurrentLinkedQueue<Long> queue = queues.get(queueKey);
            if (queue == null) {
                continue;
            }
            for (Long userId : queue) {
                candidates.add(new QueuedEntry(userId, queueKey, userEnqueueTime.getOrDefault(userId, 0L)));
            }
        }
        if (candidates.size() < requiredPlayers) {
            return Optional.empty();
        }
        candidates.sort(Comparator.comparingLong(QueuedEntry::enqueueTime));
        List<Long> matched = new ArrayList<>(requiredPlayers);
        for (int i = 0; i < requiredPlayers; i++) {
            matched.add(candidates.get(i).userId());
        }
        for (Long userId : matched) {
            String key = userQueueKey.get(userId);
            if (key != null) {
                ConcurrentLinkedQueue<Long> queue = queues.get(key);
                if (queue != null) {
                    queue.remove(userId);
                }
            }
            userQueueKey.remove(userId);
            userEnqueueTime.remove(userId);
        }
        return Optional.of(matched);
    }

    private record QueuedEntry(long userId, String queueKey, long enqueueTime) {
    }
}
