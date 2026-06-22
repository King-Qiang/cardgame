package io.github.kingqiang.cardgame.cardgamebackend.game.doudizhu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 游戏规则与牌型校验：Doudizhu。
 */
public final class DoudizhuRules {

    private static final Map<String, Integer> RANK_ORDER = new HashMap<>();

    static {
        String[] ranks = {"3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A", "2", "BJ", "RJ"};
        for (int i = 0; i < ranks.length; i++) {
            RANK_ORDER.put(ranks[i], i);
        }
    }

    private DoudizhuRules() {
    }

    public static List<String> newShuffledDeck() {
        List<String> deck = new ArrayList<>();
        String[] suits = {"S", "H", "D", "C"};
        String[] ranks = {"3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A", "2"};
        for (String rank : ranks) {
            for (String suit : suits) {
                deck.add(rank + suit);
            }
        }
        deck.add("BJ");
        deck.add("RJ");
        Collections.shuffle(deck);
        return deck;
    }

    public static int rankValue(String card) {
        if ("BJ".equals(card)) {
            return RANK_ORDER.get("BJ");
        }
        if ("RJ".equals(card)) {
            return RANK_ORDER.get("RJ");
        }
        String rank = card.startsWith("10") ? "10" : card.substring(0, 1);
        return RANK_ORDER.getOrDefault(rank, -1);
    }

    public static PlayPattern parse(List<String> cards) {
        if (cards == null || cards.isEmpty()) {
            return null;
        }
        List<String> sorted = cards.stream().sorted(Comparator.comparingInt(DoudizhuRules::rankValue)).collect(Collectors.toList());
        Map<Integer, Long> counts = sorted.stream()
                .collect(Collectors.groupingBy(DoudizhuRules::rankValue, Collectors.counting()));

        if (sorted.size() == 2 && sorted.contains("BJ") && sorted.contains("RJ")) {
            return new PlayPattern("ROCKET", 1000, sorted.size());
        }
        if (counts.size() == 1 && sorted.size() == 4) {
            return new PlayPattern("BOMB", rankValue(sorted.get(0)) + 100, sorted.size());
        }
        if (sorted.size() == 1) {
            return new PlayPattern("SINGLE", rankValue(sorted.get(0)), 1);
        }
        if (sorted.size() == 2 && counts.size() == 1) {
            return new PlayPattern("PAIR", rankValue(sorted.get(0)), 2);
        }
        if (sorted.size() == 3 && counts.size() == 1) {
            return new PlayPattern("TRIPLE", rankValue(sorted.get(0)), 3);
        }
        if (sorted.size() >= 5 && counts.size() == sorted.size()) {
            List<Integer> values = sorted.stream().map(DoudizhuRules::rankValue).sorted().collect(Collectors.toList());
            boolean consecutive = true;
            for (int i = 1; i < values.size(); i++) {
                if (values.get(i) - values.get(i - 1) != 1 || values.get(i) >= 12) {
                    consecutive = false;
                    break;
                }
            }
            if (consecutive) {
                return new PlayPattern("STRAIGHT", values.get(values.size() - 1), sorted.size());
            }
        }
        return null;
    }

    public static boolean canBeat(PlayPattern current, PlayPattern last) {
        if (current == null) {
            return false;
        }
        if (last == null) {
            return true;
        }
        if ("ROCKET".equals(current.type())) {
            return true;
        }
        if ("ROCKET".equals(last.type())) {
            return false;
        }
        if ("BOMB".equals(current.type()) && !"BOMB".equals(last.type())) {
            return true;
        }
        if ("BOMB".equals(current.type()) && "BOMB".equals(last.type())) {
            return current.mainRank() > last.mainRank();
        }
        if (!current.type().equals(last.type()) || current.length() != last.length()) {
            return false;
        }
        return current.mainRank() > last.mainRank();
    }

    public record PlayPattern(String type, int mainRank, int length) {
    }
}
