package io.github.kingqiang.cardgame.cardgamebackend.settlement.service;

import io.github.kingqiang.cardgame.cardgamebackend.bot.BotPlayerRegistry;
import io.github.kingqiang.cardgame.cardgamebackend.common.config.CardgameProperties;
import io.github.kingqiang.cardgame.cardgamebackend.game.context.GameContext;
import io.github.kingqiang.cardgame.cardgamebackend.game.engine.SettlementItem;
import io.github.kingqiang.cardgame.cardgamebackend.economy.enums.WalletTransactionType;
import io.github.kingqiang.cardgame.cardgamebackend.economy.service.WalletService;
import io.github.kingqiang.cardgame.cardgamebackend.rank.service.RankService;
import io.github.kingqiang.cardgame.cardgamebackend.record.entity.GameSettlement;
import io.github.kingqiang.cardgame.cardgamebackend.record.repository.GameSettlementRepository;
import io.github.kingqiang.cardgame.cardgamebackend.settlement.dto.SettlementEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对局结束结算：写 game_settlement、更新钱包/段位，Bot 跳过金币变动。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final CardgameProperties cardgameProperties;
    private final ObjectProvider<RedisSettlementPublisher> redisSettlementPublisher;
    private final GameSettlementRepository gameSettlementRepository;
    private final WalletService walletService;
    private final RankService rankService;
    private final BotPlayerRegistry botPlayerRegistry;

    public void dispatch(SettlementEvent event, GameContext ctx) {
        if (shouldAsync()) {
            redisSettlementPublisher.getObject().publish(event);
            log.info("Settlement queued async for record {}", event.getRecordId());
            return;
        }
        process(event, ctx);
    }

    @Transactional
    public void process(SettlementEvent event, GameContext ctx) {
        if (gameSettlementRepository.existsByRecordId(event.getRecordId())) {
            log.debug("Settlement already processed for record {}", event.getRecordId());
            return;
        }
        LocalDateTime now = event.getCreatedAt() != null ? event.getCreatedAt() : LocalDateTime.now();
        for (SettlementEvent.SettlementLine line : event.getSettlements()) {
            if (botPlayerRegistry.isBot(line.getUserId())) {
                continue;
            }
            GameSettlement settlement = new GameSettlement();
            settlement.setRecordId(event.getRecordId());
            settlement.setUserId(line.getUserId());
            settlement.setGoldDelta(line.getGoldDelta());
            settlement.setScore((int) line.getScoreDelta());
            settlement.setCreatedAt(now);
            gameSettlementRepository.save(settlement);

            if (line.getGoldDelta() != 0) {
                WalletTransactionType txType = line.getGoldDelta() > 0
                        ? WalletTransactionType.GAME_WIN
                        : WalletTransactionType.GAME_LOSE;
                walletService.changeGold(
                        line.getUserId(),
                        line.getGoldDelta(),
                        txType,
                        "GAME",
                        event.getRecordId(),
                        "对局结算"
                );
            }
        }
        List<SettlementItem> rankItems = event.getSettlements().stream()
                .filter(line -> !botPlayerRegistry.isBot(line.getUserId()))
                .map(line -> SettlementItem.builder()
                        .userId(line.getUserId())
                        .seat(line.getSeat())
                        .goldDelta(line.getGoldDelta())
                        .scoreDelta(line.getScoreDelta())
                        .build())
                .toList();
        rankService.applyGameResult(event.getRecordId(), event.getGameType(), event.getMode(),
                event.getWinnerSeat(), rankItems);
        log.info("Settlement processed for record {}", event.getRecordId());
    }

    public SettlementEvent buildEvent(String recordId, String roomId, String gameType, String mode,
                                      GameContext ctx, List<SettlementItem> settlements) {
        Integer winnerSeat = ctx.getWinnerSeat();
        return SettlementEvent.builder()
                .recordId(recordId)
                .roomId(roomId)
                .gameType(gameType)
                .mode(mode)
                .winnerSeat(winnerSeat)
                .createdAt(LocalDateTime.now())
                .settlements(settlements.stream()
                        .map(item -> SettlementEvent.SettlementLine.builder()
                                .userId(item.getUserId())
                                .seat(item.getSeat())
                                .goldDelta(item.getGoldDelta())
                                .scoreDelta(item.getScoreDelta())
                                .build())
                        .toList())
                .build();
    }

    public boolean shouldAsync() {
        return cardgameProperties.redis().enabled() && cardgameProperties.settlement().asyncEnabled();
    }
}
