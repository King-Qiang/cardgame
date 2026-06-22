package io.github.kingqiang.cardgame.cardgamebackend.room.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kingqiang.cardgame.cardgamebackend.game.engine.SettlementItem;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SettlementSerializationTest {

    @Test
    void settlementItemSerializesWithUserIdAndSeat() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<SettlementItem> settlements = List.of(
                SettlementItem.builder()
                        .userId(10001L)
                        .seat(1)
                        .scoreDelta(10L)
                        .goldDelta(10L)
                        .multiplier(1)
                        .build()
        );
        Map<String, Object> result = new HashMap<>();
        result.put("winnerSeat", 1);
        Map<String, Object> payload = Map.of("settlements", settlements, "result", result);

        String json = mapper.writeValueAsString(payload);

        assertTrue(json.contains("\"userId\":10001"), json);
        assertTrue(json.contains("\"seat\":1"), json);
        assertTrue(json.contains("\"goldDelta\":10"), json);
        assertTrue(json.contains("\"winnerSeat\":1"), json);
    }
}
