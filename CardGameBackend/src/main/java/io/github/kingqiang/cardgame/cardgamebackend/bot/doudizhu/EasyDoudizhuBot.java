package io.github.kingqiang.cardgame.cardgamebackend.bot.doudizhu;

import io.github.kingqiang.cardgame.cardgamebackend.bot.BotDifficulty;
import io.github.kingqiang.cardgame.cardgamebackend.game.context.GameContext;
import io.github.kingqiang.cardgame.cardgamebackend.game.doudizhu.DoudizhuRules;
import io.github.kingqiang.cardgame.cardgamebackend.game.engine.GameAction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 简单斗地主 Bot：随机合法出牌 / pass。
 */
@Component
public class EasyDoudizhuBot implements DoudizhuBot {

    @Override
    public GameAction decide(GameContext ctx, int seat, BotDifficulty difficulty) {
        long userId = ctx.userIdOfSeat(seat);
        if ("BIDDING".equals(ctx.getPhase())) {
            String action = ThreadLocalRandom.current().nextDouble() < 0.35 ? "CALL_LANDLORD" : "PASS";
            return GameAction.builder().action(action).userId(userId).seat(seat).build();
        }
        List<String> cards = findMinimalPlay(ctx, seat);
        if (cards != null && !cards.isEmpty()) {
            return GameAction.builder().action("PLAY_CARDS").userId(userId).seat(seat).cards(cards).build();
        }
        return GameAction.builder().action("PASS").userId(userId).seat(seat).build();
    }

    private List<String> findMinimalPlay(GameContext ctx, int seat) {
        List<String> hand = new ArrayList<>(ctx.getHands().getOrDefault(seat, List.of()));
        if (hand.isEmpty()) {
            return null;
        }
        hand.sort(Comparator.comparingInt(DoudizhuRules::rankValue));
        DoudizhuRules.PlayPattern last = parseLastPlay(ctx.getLastPlay());

        List<String> rocket = tryPattern(hand, List.of("BJ", "RJ"), last);
        if (rocket != null) {
            return rocket;
        }

        Map<Integer, List<String>> byRank = groupByRank(hand);
        for (List<String> sameRank : byRank.values()) {
            if (sameRank.size() == 4) {
                List<String> bomb = tryPattern(hand, sameRank, last);
                if (bomb != null) {
                    return bomb;
                }
            }
        }

        for (String card : hand) {
            List<String> single = tryPattern(hand, List.of(card), last);
            if (single != null) {
                return single;
            }
        }

        for (List<String> sameRank : byRank.values()) {
            if (sameRank.size() >= 2) {
                List<String> pair = tryPattern(hand, sameRank.subList(0, 2), last);
                if (pair != null) {
                    return pair;
                }
            }
        }

        for (List<String> sameRank : byRank.values()) {
            if (sameRank.size() >= 3) {
                List<String> triple = tryPattern(hand, sameRank.subList(0, 3), last);
                if (triple != null) {
                    return triple;
                }
            }
        }

        List<String> straight = findStraight(hand, last);
        if (straight != null) {
            return straight;
        }
        return null;
    }

    private List<String> findStraight(List<String> hand, DoudizhuRules.PlayPattern last) {
        List<Integer> ranks = hand.stream()
                .map(DoudizhuRules::rankValue)
                .filter(v -> v >= 0 && v < 12)
                .distinct()
                .sorted()
                .toList();
        for (int len = 5; len <= ranks.size(); len++) {
            for (int start = 0; start + len <= ranks.size(); start++) {
                boolean consecutive = true;
                for (int i = 1; i < len; i++) {
                    if (ranks.get(start + i) - ranks.get(start + i - 1) != 1) {
                        consecutive = false;
                        break;
                    }
                }
                if (!consecutive) {
                    continue;
                }
                List<String> cards = new ArrayList<>();
                for (int i = 0; i < len; i++) {
                    int rankValue = ranks.get(start + i);
                    String card = hand.stream()
                            .filter(c -> DoudizhuRules.rankValue(c) == rankValue)
                            .findFirst()
                            .orElse(null);
                    if (card == null) {
                        cards = null;
                        break;
                    }
                    cards.add(card);
                }
                if (cards != null) {
                    List<String> play = tryPattern(hand, cards, last);
                    if (play != null) {
                        return play;
                    }
                }
            }
        }
        return null;
    }

    private List<String> tryPattern(List<String> hand, List<String> cards, DoudizhuRules.PlayPattern last) {
        if (!hand.containsAll(cards)) {
            return null;
        }
        DoudizhuRules.PlayPattern pattern = DoudizhuRules.parse(cards);
        if (pattern == null) {
            return null;
        }
        if (!DoudizhuRules.canBeat(pattern, last)) {
            return null;
        }
        return new ArrayList<>(cards);
    }

    private Map<Integer, List<String>> groupByRank(List<String> hand) {
        Map<Integer, List<String>> grouped = new HashMap<>();
        for (String card : hand) {
            grouped.computeIfAbsent(DoudizhuRules.rankValue(card), k -> new ArrayList<>()).add(card);
        }
        return grouped;
    }

    private DoudizhuRules.PlayPattern parseLastPlay(Map<String, Object> lastPlay) {
        if (lastPlay == null) {
            return null;
        }
        Object type = lastPlay.get("type");
        Object mainRank = lastPlay.get("mainRank");
        Object length = lastPlay.get("length");
        if (type == null || mainRank == null || length == null) {
            return null;
        }
        return new DoudizhuRules.PlayPattern(
                String.valueOf(type),
                ((Number) mainRank).intValue(),
                ((Number) length).intValue()
        );
    }
}
