package io.github.kingqiang.cardgame.cardgamebackend.settlement;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kingqiang.cardgame.cardgamebackend.common.config.CardgameProperties;
import io.github.kingqiang.cardgame.cardgamebackend.game.context.GameContext;
import io.github.kingqiang.cardgame.cardgamebackend.game.context.GameContextStore;
import io.github.kingqiang.cardgame.cardgamebackend.settlement.dto.SettlementEvent;
import io.github.kingqiang.cardgame.cardgamebackend.settlement.service.SettlementService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 定时任务：SettlementConsumer。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "cardgame.settlement", name = "async-enabled", havingValue = "true")
public class SettlementConsumerJob {

    private final StringRedisTemplate redisTemplate;
    private final CardgameProperties cardgameProperties;
    private final SettlementService settlementService;
    private final GameContextStore gameContextStore;
    private final ObjectMapper objectMapper;

    @PostConstruct
    void initConsumerGroup() {
        if (!cardgameProperties.redis().enabled()) {
            return;
        }
        String stream = cardgameProperties.settlement().streamName();
        String group = cardgameProperties.settlement().consumerGroup();
        try {
            redisTemplate.opsForStream().createGroup(stream, ReadOffset.from("0-0"), group);
            log.info("Created settlement consumer group {}", group);
        } catch (Exception ex) {
            log.debug("Settlement consumer group may already exist: {}", ex.getMessage());
        }
    }

    @Scheduled(fixedDelayString = "${cardgame.job.settlement-consume-interval-ms:2000}")
    public void consume() {
        if (!cardgameProperties.redis().enabled()) {
            return;
        }
        String stream = cardgameProperties.settlement().streamName();
        String group = cardgameProperties.settlement().consumerGroup();
        String consumer = cardgameProperties.settlement().consumerName();
        try {
            List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream().read(
                    Consumer.from(group, consumer),
                    StreamOffset.create(stream, ReadOffset.lastConsumed())
            );
            if (records == null || records.isEmpty()) {
                return;
            }
            for (MapRecord<String, Object, Object> record : records) {
                handleRecord(stream, group, record);
            }
        } catch (Exception ex) {
            log.warn("Settlement consumer error: {}", ex.getMessage());
        }
    }

    private void handleRecord(String stream, String group, MapRecord<String, Object, Object> record) {
        try {
            Object payloadObj = record.getValue().get("payload");
            if (payloadObj == null) {
                ack(stream, group, record);
                return;
            }
            SettlementEvent event = objectMapper.readValue(String.valueOf(payloadObj), SettlementEvent.class);
            GameContext ctx = gameContextStore.getByRoom(event.getRoomId());
            settlementService.process(event, ctx);
            ack(stream, group, record);
        } catch (Exception ex) {
            log.error("Failed to process settlement record {}: {}", record.getId(), ex.getMessage());
        }
    }

    private void ack(String stream, String group, MapRecord<String, Object, Object> record) {
        redisTemplate.opsForStream().acknowledge(stream, group, record.getId());
    }
}
