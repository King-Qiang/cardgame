package io.github.kingqiang.cardgame.cardgamebackend.game.context;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GameContext。
 */
@Getter
@Setter
public class GameContext {

    private String recordId;
    private String roomId;
    private String gameType;
    private String phase = "WAITING";
    private int currentSeat;
    private int landlordSeat = -1;
    private int multiplier = 1;
    private int actionSeq;
    private List<String> bottomCards = new ArrayList<>();
    private Map<Integer, List<String>> hands = new HashMap<>();
    private Map<Integer, Long> seatUser = new HashMap<>();
    private Map<String, Object> lastPlay;
    private int passCount;
    private Integer winnerSeat;
    private int baseScore = 1;

    public long userIdOfSeat(int seat) {
        return seatUser.getOrDefault(seat, 0L);
    }

    public int seatOfUser(long userId) {
        for (Map.Entry<Integer, Long> entry : seatUser.entrySet()) {
            if (entry.getValue().equals(userId)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public void nextSeat() {
        currentSeat = (currentSeat + 1) % seatUser.size();
    }
}
