package io.github.kingqiang.cardgame.cardgamebackend.rank.service;

import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageRequest;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageResult;
import io.github.kingqiang.cardgame.cardgamebackend.game.context.GameContext;
import io.github.kingqiang.cardgame.cardgamebackend.game.engine.SettlementItem;
import io.github.kingqiang.cardgame.cardgamebackend.rank.dto.PlayerRankDto;
import io.github.kingqiang.cardgame.cardgamebackend.rank.dto.PlayerRankLogDto;
import io.github.kingqiang.cardgame.cardgamebackend.rank.dto.RankLeaderboardItemDto;
import io.github.kingqiang.cardgame.cardgamebackend.rank.entity.PlayerRank;
import io.github.kingqiang.cardgame.cardgamebackend.rank.entity.PlayerRankLog;
import io.github.kingqiang.cardgame.cardgamebackend.rank.enums.RankTier;
import io.github.kingqiang.cardgame.cardgamebackend.rank.repository.PlayerRankLogRepository;
import io.github.kingqiang.cardgame.cardgamebackend.rank.repository.PlayerRankRepository;
import io.github.kingqiang.cardgame.cardgamebackend.economy.entity.SystemConfig;
import io.github.kingqiang.cardgame.cardgamebackend.economy.repository.SystemConfigRepository;
import io.github.kingqiang.cardgame.cardgamebackend.user.entity.Player;
import io.github.kingqiang.cardgame.cardgamebackend.user.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 段位积分变更、升降段判定与排行榜查询。
 */
@Service
@RequiredArgsConstructor
public class RankService {

    public static final String RANK_THRESHOLDS_KEY = "rank.tier_thresholds";

    private final PlayerRankRepository playerRankRepository;
    private final PlayerRankLogRepository playerRankLogRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final PlayerRepository playerRepository;

    @Transactional(readOnly = true)
    public PlayerRankDto getMyRank(long userId, String gameType) {
        return toDto(getOrCreateRank(userId, gameType));
    }

    @Transactional(readOnly = true)
    public String getTier(long userId, String gameType) {
        return getOrCreateRank(userId, gameType).getTier();
    }

    @Transactional(readOnly = true)
    public PageResult<RankLeaderboardItemDto> leaderboard(
            String gameType, String seasonId, String tier, PageRequest pageRequest) {
        String resolvedSeason = seasonId != null && !seasonId.isBlank() ? seasonId : currentSeasonId();
        Page<PlayerRank> page;
        if (tier != null && !tier.isBlank()) {
            page = playerRankRepository.findByGameTypeAndSeasonIdAndTierOrderByPointsDesc(
                    gameType, resolvedSeason, tier.toUpperCase(),
                    org.springframework.data.domain.PageRequest.of(
                            pageRequest.getPage() - 1, pageRequest.getPageSize()));
        } else {
            page = playerRankRepository.findByGameTypeAndSeasonIdOrderByPointsDesc(
                    gameType, resolvedSeason,
                    org.springframework.data.domain.PageRequest.of(
                            pageRequest.getPage() - 1, pageRequest.getPageSize()));
        }
        int baseRank = (pageRequest.getPage() - 1) * pageRequest.getPageSize();
        List<Long> userIds = page.getContent().stream().map(PlayerRank::getUserId).toList();
        Map<Long, String> nicknames = playerRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(Player::getId, Player::getNickname));
        List<RankLeaderboardItemDto> list = new ArrayList<>();
        int i = 0;
        for (PlayerRank rank : page.getContent()) {
            list.add(RankLeaderboardItemDto.builder()
                    .rank(baseRank + i + 1)
                    .userId(rank.getUserId())
                    .nickname(nicknames.getOrDefault(rank.getUserId(), "-"))
                    .tier(rank.getTier())
                    .points(rank.getPoints())
                    .wins(rank.getWins())
                    .losses(rank.getLosses())
                    .build());
            i++;
        }
        return PageResult.of(list, page.getTotalElements(), pageRequest.getPage(), pageRequest.getPageSize());
    }

    @Transactional(readOnly = true)
    public Map<Long, PlayerRank> findRanksForUsers(List<Long> userIds, String gameType) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        String seasonId = currentSeasonId();
        return playerRankRepository.findByUserIdInAndGameTypeAndSeasonId(userIds, gameType, seasonId).stream()
                .collect(Collectors.toMap(PlayerRank::getUserId, r -> r, (a, b) -> a, HashMap::new));
    }

    @Transactional(readOnly = true)
    public List<PlayerRankLogDto> recentLogs(long userId, String gameType) {
        return playerRankLogRepository.findTop5ByUserIdAndGameTypeOrderByCreatedAtDesc(userId, gameType).stream()
                .map(this::toLogDto)
                .toList();
    }

    @Transactional
    public PlayerRank getOrCreateRank(long userId, String gameType) {
        String seasonId = currentSeasonId();
        return playerRankRepository.findByUserIdAndGameTypeAndSeasonId(userId, gameType, seasonId)
                .orElseGet(() -> {
                    LocalDateTime now = LocalDateTime.now();
                    PlayerRank rank = new PlayerRank();
                    rank.setUserId(userId);
                    rank.setGameType(gameType);
                    rank.setSeasonId(seasonId);
                    rank.setTier(RankTier.BRONZE.name());
                    rank.setPoints(0);
                    rank.setWins(0);
                    rank.setLosses(0);
                    rank.setUpdatedAt(now);
                    return playerRankRepository.save(rank);
                });
    }

    @Transactional
    public void applyGameResult(String recordId, String gameType, String roomMode,
                                GameContext ctx, List<SettlementItem> settlements) {
        applyGameResult(recordId, gameType, roomMode,
                ctx != null ? ctx.getWinnerSeat() : null, settlements);
    }

    @Transactional
    public void applyGameResult(String recordId, String gameType, String roomMode,
                                Integer winnerSeat, List<SettlementItem> settlements) {
        if (!"MATCH".equals(roomMode) && !"RANKED".equals(roomMode)) {
            return;
        }
        RankConfig config = loadRankConfig();
        for (SettlementItem item : settlements) {
            boolean win = winnerSeat != null && item.getSeat() == winnerSeat;
            applyResultForUser(recordId, gameType, item.getUserId(), win, config);
        }
    }

    private void applyResultForUser(String recordId, String gameType, long userId, boolean win, RankConfig config) {
        PlayerRank rank = getOrCreateRank(userId, gameType);
        int delta = win ? config.winPoints() : -config.losePoints();
        int pointsBefore = rank.getPoints();
        String tierBefore = rank.getTier();
        int pointsAfter = Math.max(0, pointsBefore + delta);
        String tierAfter = config.tierForPoints(pointsAfter).name();

        rank.setPoints(pointsAfter);
        rank.setTier(tierAfter);
        if (win) {
            rank.setWins(rank.getWins() + 1);
        } else {
            rank.setLosses(rank.getLosses() + 1);
        }
        rank.setUpdatedAt(LocalDateTime.now());
        playerRankRepository.save(rank);

        PlayerRankLog log = new PlayerRankLog();
        log.setUserId(userId);
        log.setRecordId(recordId);
        log.setGameType(gameType);
        log.setSeasonId(rank.getSeasonId());
        log.setDeltaPoints(delta);
        log.setTierBefore(tierBefore);
        log.setTierAfter(tierAfter);
        log.setPointsBefore(pointsBefore);
        log.setPointsAfter(pointsAfter);
        log.setCreatedAt(LocalDateTime.now());
        playerRankLogRepository.save(log);
    }

    public String currentSeasonId() {
        LocalDateTime now = LocalDateTime.now();
        int quarter = (now.getMonthValue() - 1) / 3 + 1;
        return "S" + now.getYear() + "Q" + quarter;
    }

    private RankConfig loadRankConfig() {
        SystemConfig config = systemConfigRepository.findById(RANK_THRESHOLDS_KEY).orElse(null);
        if (config == null || !(config.getConfigValue() instanceof Map<?, ?> value)) {
            return RankConfig.defaults();
        }
        int winPoints = readInt(value.get("winPoints"), 25);
        int losePoints = readInt(value.get("losePoints"), 15);
        List<TierThreshold> tiers = new ArrayList<>();
        Object tiersObj = value.get("tiers");
        if (tiersObj instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    String tier = String.valueOf(map.get("tier"));
                    int min = readInt(map.get("min"), 0);
                    Integer max = map.get("max") instanceof Number n ? n.intValue() : null;
                    tiers.add(new TierThreshold(RankTier.fromString(tier), min, max));
                }
            }
        }
        if (tiers.isEmpty()) {
            return RankConfig.defaults();
        }
        tiers.sort(Comparator.comparingInt(TierThreshold::min));
        return new RankConfig(winPoints, losePoints, tiers);
    }

    private int readInt(Object value, int defaultValue) {
        return value instanceof Number n ? n.intValue() : defaultValue;
    }

    private PlayerRankDto toDto(PlayerRank rank) {
        RankConfig config = loadRankConfig();
        RankTier current = RankTier.fromString(rank.getTier());
        RankTier next = config.nextTier(current);
        Integer pointsToNext = next != null ? config.minPoints(next) - rank.getPoints() : null;
        return PlayerRankDto.builder()
                .userId(rank.getUserId())
                .gameType(rank.getGameType())
                .seasonId(rank.getSeasonId())
                .tier(rank.getTier())
                .points(rank.getPoints())
                .wins(rank.getWins())
                .losses(rank.getLosses())
                .nextTier(next != null ? next.name() : null)
                .pointsToNextTier(pointsToNext != null ? Math.max(0, pointsToNext) : null)
                .build();
    }

    private PlayerRankLogDto toLogDto(PlayerRankLog log) {
        return PlayerRankLogDto.builder()
                .id(log.getId())
                .recordId(log.getRecordId())
                .deltaPoints(log.getDeltaPoints())
                .tierBefore(log.getTierBefore())
                .tierAfter(log.getTierAfter())
                .pointsBefore(log.getPointsBefore())
                .pointsAfter(log.getPointsAfter())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private record TierThreshold(RankTier tier, int min, Integer max) {
    }

    private record RankConfig(int winPoints, int losePoints, List<TierThreshold> tiers) {

        static RankConfig defaults() {
            return new RankConfig(25, 15, List.of(
                    new TierThreshold(RankTier.BRONZE, 0, 99),
                    new TierThreshold(RankTier.SILVER, 100, 299),
                    new TierThreshold(RankTier.GOLD, 300, 599),
                    new TierThreshold(RankTier.PLATINUM, 600, 999),
                    new TierThreshold(RankTier.DIAMOND, 1000, null)
            ));
        }

        RankTier tierForPoints(int points) {
            RankTier result = RankTier.BRONZE;
            for (TierThreshold threshold : tiers) {
                if (points >= threshold.min()) {
                    result = threshold.tier();
                }
            }
            return result;
        }

        RankTier nextTier(RankTier current) {
            boolean found = false;
            for (TierThreshold threshold : tiers) {
                if (found) {
                    return threshold.tier();
                }
                if (threshold.tier() == current) {
                    found = true;
                }
            }
            return null;
        }

        int minPoints(RankTier tier) {
            for (TierThreshold threshold : tiers) {
                if (threshold.tier() == tier) {
                    return threshold.min();
                }
            }
            return 0;
        }
    }
}
