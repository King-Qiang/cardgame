package io.github.kingqiang.cardgame.cardgamebackend.room.websocket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kingqiang.cardgame.cardgamebackend.common.config.CardgameProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;

/**
 * 消息中继：RedisWebSocket。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "cardgame.redis", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class RedisWebSocketRelay implements MessageListener {

    private final StringRedisTemplate redisTemplate;
    private final RedisMessageListenerContainer listenerContainer;
    private final GameMessageBroadcaster gameMessageBroadcaster;
    private final ObjectMapper objectMapper;
    private final CardgameProperties cardgameProperties;

    @PostConstruct
    void subscribe() {
        listenerContainer.addMessageListener(this, new PatternTopic(cardgameProperties.redis().wsChannelPrefix() + "*"));
    }

    public void publish(String roomId, Map<String, Object> message) {
        try {
            String channel = cardgameProperties.redis().wsChannelPrefix() + roomId;
            redisTemplate.convertAndSend(channel, objectMapper.writeValueAsString(message));
        } catch (Exception ex) {
            log.warn("Failed to publish WS message to Redis roomId={}", roomId, ex);
        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            Map<String, Object> payload = objectMapper.readValue(message.getBody(),
                    new TypeReference<Map<String, Object>>() {
                    });
            Object roomId = payload.get("roomId");
            if (roomId instanceof String room) {
                gameMessageBroadcaster.deliverLocalRoomMessage(room, payload);
            }
        } catch (Exception ex) {
            log.warn("Failed to handle Redis WS relay message", ex);
        }
    }
}
