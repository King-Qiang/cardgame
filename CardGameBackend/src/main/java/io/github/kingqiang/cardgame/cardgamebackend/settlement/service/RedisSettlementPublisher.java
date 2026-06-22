package io.github.kingqiang.cardgame.cardgamebackend.settlement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kingqiang.cardgame.cardgamebackend.common.config.CardgameProperties;
import io.github.kingqiang.cardgame.cardgamebackend.settlement.dto.SettlementEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 消息发布：RedisSettlement。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "cardgame.redis", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class RedisSettlementPublisher {

    private final StringRedisTemplate redisTemplate;
    private final CardgameProperties cardgameProperties;
    private final ObjectMapper objectMapper;

    public void publish(SettlementEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            String stream = cardgameProperties.settlement().streamName();
            RecordId id = redisTemplate.opsForStream().add(stream, Map.of("payload", payload));
            log.debug("Published settlement event {} to stream {}", event.getRecordId(), id);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize settlement event", ex);
        }
    }
}
