package io.github.kingqiang.cardgame.cardgamebackend.game.doudizhu;

import io.github.kingqiang.cardgame.cardgamebackend.game.context.GameContext;
import io.github.kingqiang.cardgame.cardgamebackend.game.engine.ActionResult;
import io.github.kingqiang.cardgame.cardgamebackend.game.engine.GameAction;
import io.github.kingqiang.cardgame.cardgamebackend.game.engine.GameEngine;
import io.github.kingqiang.cardgame.cardgamebackend.game.engine.GameType;
import io.github.kingqiang.cardgame.cardgamebackend.game.engine.SettlementItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 斗地主游戏引擎：叫地主、出牌校验、可见状态裁剪与胜负结算。
 */
@Component
public class DoudizhuEngine implements GameEngine {

    @Override
    public GameType type() {
        return GameType.DOUDIZHU;
    }

    @Override
    public void onPlayerJoin(GameContext ctx, long userId, int seat) {
        ctx.getSeatUser().put(seat, userId);
    }

    @Override
    public void onPlayerLeave(GameContext ctx, long userId) {
        int seat = ctx.seatOfUser(userId);
        if (seat >= 0) {
            ctx.getSeatUser().remove(seat);
            ctx.getHands().remove(seat);
        }
    }

    @Override
    public boolean canStart(GameContext ctx) {
        return ctx.getSeatUser().size() == 3;
    }

    @Override
    public void onGameStart(GameContext ctx) {
        List<String> deck = DoudizhuRules.newShuffledDeck();
        ctx.getHands().clear();
        for (int seat = 0; seat < 3; seat++) {
            ctx.getHands().put(seat, new ArrayList<>(deck.subList(seat * 17, seat * 17 + 17)));
        }
        ctx.setBottomCards(new ArrayList<>(deck.subList(51, 54)));
        ctx.setPhase("BIDDING");
        ctx.setCurrentSeat(0);
        ctx.setLandlordSeat(-1);
        ctx.setLastPlay(null);
        ctx.setPassCount(0);
        ctx.setWinnerSeat(null);
    }

    @Override
    public ActionResult handleAction(GameContext ctx, GameAction action) {
        int seat = ctx.seatOfUser(action.getUserId());
        if (seat < 0) {
            return ActionResult.fail("不在对局中");
        }
        if (seat != ctx.getCurrentSeat()) {
            return ActionResult.fail("未轮到该玩家");
        }

        return switch (ctx.getPhase()) {
            case "BIDDING" -> handleBidding(ctx, seat, action);
            case "PLAYING" -> handlePlaying(ctx, seat, action);
            default -> ActionResult.fail("当前阶段不可操作");
        };
    }

    private ActionResult handleBidding(GameContext ctx, int seat, GameAction action) {
        if ("CALL_LANDLORD".equals(action.getAction())) {
            ctx.setLandlordSeat(seat);
            becomeLandlord(ctx);
            return ActionResult.ok();
        }
        if ("PASS".equals(action.getAction())) {
            ctx.nextSeat();
            if (ctx.getCurrentSeat() == 0 && ctx.getLandlordSeat() < 0) {
                ctx.setLandlordSeat(0);
                becomeLandlord(ctx);
            } else if (ctx.getLandlordSeat() >= 0 && ctx.getCurrentSeat() == ctx.getLandlordSeat()) {
                becomeLandlord(ctx);
            }
            return ActionResult.ok();
        }
        return ActionResult.fail("非法叫地主操作");
    }

    private void becomeLandlord(GameContext ctx) {
        int landlord = ctx.getLandlordSeat();
        List<String> hand = new ArrayList<>(ctx.getHands().get(landlord));
        hand.addAll(ctx.getBottomCards());
        hand.sort(Comparator.comparingInt(DoudizhuRules::rankValue));
        ctx.getHands().put(landlord, hand);
        ctx.setPhase("PLAYING");
        ctx.setCurrentSeat(landlord);
        ctx.setLastPlay(null);
        ctx.setPassCount(0);
    }

    private ActionResult handlePlaying(GameContext ctx, int seat, GameAction action) {
        if ("PASS".equals(action.getAction())) {
            if (ctx.getLastPlay() == null) {
                return ActionResult.fail("首家不能过");
            }
            ctx.setPassCount(ctx.getPassCount() + 1);
            ctx.nextSeat();
            if (ctx.getPassCount() >= 2) {
                ctx.setLastPlay(null);
                ctx.setPassCount(0);
            }
            return ActionResult.ok();
        }
        if ("PLAY_CARDS".equals(action.getAction())) {
            List<String> cards = action.getCards();
            if (cards == null || cards.isEmpty()) {
                return ActionResult.fail("出牌不能为空");
            }
            List<String> hand = new ArrayList<>(ctx.getHands().get(seat));
            for (String card : cards) {
                if (!hand.remove(card)) {
                    return ActionResult.fail("手牌不存在: " + card);
                }
            }
            DoudizhuRules.PlayPattern pattern = DoudizhuRules.parse(cards);
            DoudizhuRules.PlayPattern last = ctx.getLastPlay() == null ? null :
                    new DoudizhuRules.PlayPattern(
                            String.valueOf(ctx.getLastPlay().get("type")),
                            ((Number) ctx.getLastPlay().get("mainRank")).intValue(),
                            ((Number) ctx.getLastPlay().get("length")).intValue()
                    );
            if (!DoudizhuRules.canBeat(pattern, last)) {
                return ActionResult.fail("牌型不合法或无法压过上家");
            }
            ctx.getHands().put(seat, hand);
            Map<String, Object> lastPlay = new HashMap<>();
            lastPlay.put("seat", seat);
            lastPlay.put("cards", cards);
            lastPlay.put("type", pattern.type());
            lastPlay.put("mainRank", pattern.mainRank());
            lastPlay.put("length", pattern.length());
            ctx.setLastPlay(lastPlay);
            ctx.setPassCount(0);
            if (hand.isEmpty()) {
                ctx.setWinnerSeat(seat);
                ctx.setPhase("FINISHED");
                return ActionResult.finished();
            }
            ctx.nextSeat();
            return ActionResult.ok();
        }
        return ActionResult.fail("非法出牌操作");
    }

    @Override
    public List<SettlementItem> settle(GameContext ctx) {
        int winner = ctx.getWinnerSeat() != null ? ctx.getWinnerSeat() : ctx.getLandlordSeat();
        int landlord = ctx.getLandlordSeat();
        boolean landlordWin = winner == landlord;
        long base = ctx.getBaseScore() * ctx.getMultiplier();
        List<SettlementItem> items = new ArrayList<>();
        for (Map.Entry<Integer, Long> entry : ctx.getSeatUser().entrySet()) {
            int seat = entry.getKey();
            long userId = entry.getValue();
            boolean isLandlord = seat == landlord;
            long goldDelta;
            if (isLandlord) {
                goldDelta = landlordWin ? base * 2 : -base * 2;
            } else {
                goldDelta = landlordWin ? -base : base;
            }
            items.add(SettlementItem.builder()
                    .userId(userId)
                    .seat(seat)
                    .scoreDelta(goldDelta)
                    .goldDelta(goldDelta)
                    .multiplier(ctx.getMultiplier())
                    .build());
        }
        return items;
    }

    @Override
    public Object getVisibleState(GameContext ctx, long userId) {
        int mySeat = ctx.seatOfUser(userId);
        Map<String, Object> state = new HashMap<>();
        state.put("recordId", ctx.getRecordId());
        state.put("roomId", ctx.getRoomId());
        state.put("gameType", ctx.getGameType());
        state.put("phase", ctx.getPhase());
        state.put("currentSeat", ctx.getCurrentSeat());
        state.put("landlordSeat", ctx.getLandlordSeat());
        state.put("multiplier", ctx.getMultiplier());
        state.put("actionSeq", ctx.getActionSeq());
        if (mySeat >= 0) {
            state.put("myHand", ctx.getHands().get(mySeat));
            state.put("mySeat", mySeat);
        }
        if (ctx.getLastPlay() != null) {
            Map<String, Object> last = new HashMap<>(ctx.getLastPlay());
            Object cardsObj = last.get("cards");
            if (cardsObj instanceof List<?> cards) {
                last.put("cardCount", cards.size());
            } else if (!last.containsKey("cardCount")) {
                last.put("cardCount", 0);
            }
            state.put("lastPlay", last);
        }
        state.put("handCounts", ctx.getHands().entrySet().stream()
                .collect(Collectors.toMap(e -> String.valueOf(e.getKey()), e -> e.getValue().size())));
        return state;
    }
}
