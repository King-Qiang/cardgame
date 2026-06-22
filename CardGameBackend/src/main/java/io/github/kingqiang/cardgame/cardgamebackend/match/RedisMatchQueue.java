package io.github.kingqiang.cardgame.cardgamebackend.match;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * RedisMatchQueue。
 */
@Component
@ConditionalOnProperty(prefix = "cardgame.redis", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class RedisMatchQueue implements MatchQueue {

    private static final String QUEUE_KEY_PREFIX = "match:queue:";
    private static final String USER_KEY_PREFIX = "match:user:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void enqueue(String queueKey, long userId) {
        long now = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(fullQueueKey(queueKey), String.valueOf(userId), now);
        redisTemplate.opsForHash().put(userKey(userId), "queueKey", queueKey);
        redisTemplate.opsForHash().put(userKey(userId), "enqueuedAt", String.valueOf(now));
        redisTemplate.expire(userKey(userId), 30, TimeUnit.MINUTES);
    }

    @Override
    public void cancel(long userId) {
        Object queueKey = redisTemplate.opsForHash().get(userKey(userId), "queueKey");
        if (queueKey != null) {
            redisTemplate.opsForZSet().remove(fullQueueKey(String.valueOf(queueKey)), String.valueOf(userId));
        }
        redisTemplate.delete(userKey(userId));
    }

    @Override
    public boolean isQueued(long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(userKey(userId)));
    }

    @Override
    public String getQueueKey(long userId) {
        Object value = redisTemplate.opsForHash().get(userKey(userId), "queueKey");
        return value != null ? String.valueOf(value) : null;
    }

    @Override
    public long getEnqueueTime(long userId) {
        Object value = redisTemplate.opsForHash().get(userKey(userId), "enqueuedAt");
        if (value == null) {
            return System.currentTimeMillis();
        }
        return Long.parseLong(String.valueOf(value));
    }

    @Override
    public int queueSize(String queueKey) {
        Long size = redisTemplate.opsForZSet().size(fullQueueKey(queueKey));
        return size == null ? 0 : size.intValue();
    }

    @Override
    public Optional<List<Long>> pollMatchedPlayers(String queueKey, int requiredPlayers) {
        return pollMatchedPlayersFromKeys(List.of(queueKey), requiredPlayers);
    }

    @Override
    public Optional<List<Long>> pollMatchedPlayersFromKeys(List<String> queueKeys, int requiredPlayers) {
        List<QueuedEntry> candidates = new ArrayList<>();
        for (String queueKey : queueKeys) {
            Set<ZSetOperations.TypedTuple<String>> tuples =
                    redisTemplate.opsForZSet().rangeWithScores(fullQueueKey(queueKey), 0, -1);
            if (tuples == null) {
                continue;
            }
            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                if (tuple.getValue() == null || tuple.getScore() == null) {
                    continue;
                }
                candidates.add(new QueuedEntry(
                        Long.parseLong(tuple.getValue()), queueKey, tuple.getScore().longValue()));
            }
        }
        if (candidates.size() < requiredPlayers) {
            return Optional.empty();
        }
        candidates.sort(Comparator.comparingLong(QueuedEntry::enqueueTime));
        List<Long> matched = candidates.stream().limit(requiredPlayers).map(QueuedEntry::userId).toList();
        for (Long userId : matched) {
            String key = getQueueKey(userId);
            if (key != null) {
                redisTemplate.opsForZSet().remove(fullQueueKey(key), String.valueOf(userId));
            }
            redisTemplate.delete(userKey(userId));
        }
        return Optional.of(new ArrayList<>(matched));
    }

    private String fullQueueKey(String queueKey) {
        return QUEUE_KEY_PREFIX + queueKey;
    }

    private String userKey(long userId) {
        return USER_KEY_PREFIX + userId;
    }

    private record QueuedEntry(long userId, String queueKey, long enqueueTime) {
    }
}
