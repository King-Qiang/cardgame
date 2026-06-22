package io.github.kingqiang.cardgame.cardgamebackend.user.service;

import io.github.kingqiang.cardgame.cardgamebackend.common.exception.BusinessException;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import io.github.kingqiang.cardgame.cardgamebackend.economy.service.WalletService;
import io.github.kingqiang.cardgame.cardgamebackend.rank.dto.PlayerRankDto;
import io.github.kingqiang.cardgame.cardgamebackend.rank.service.RankService;
import io.github.kingqiang.cardgame.cardgamebackend.user.dto.PlayerMeDto;
import io.github.kingqiang.cardgame.cardgamebackend.user.dto.RankSummaryDto;
import io.github.kingqiang.cardgame.cardgamebackend.user.dto.UpdatePlayerProfileRequest;
import io.github.kingqiang.cardgame.cardgamebackend.user.entity.Player;
import io.github.kingqiang.cardgame.cardgamebackend.user.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 业务服务：PlayerUser 领域逻辑。
 */
@Service
@RequiredArgsConstructor
public class PlayerUserService {

    private final PlayerRepository playerRepository;
    private final WalletService walletService;
    private final RankService rankService;

    @Transactional(readOnly = true)
    public PlayerMeDto getMe(long userId, String gameType) {
        Player player = requirePlayer(userId);
        PlayerRankDto rank = rankService.getMyRank(userId, gameType);
        return PlayerMeDto.builder()
                .id(player.getId())
                .nickname(player.getNickname())
                .avatar(player.getAvatar())
                .gold(walletService.getGoldBalance(userId))
                .createdAt(player.getCreatedAt())
                .rankSummary(RankSummaryDto.builder()
                        .gameType(rank.getGameType())
                        .tier(rank.getTier())
                        .points(rank.getPoints())
                        .wins(rank.getWins())
                        .losses(rank.getLosses())
                        .build())
                .build();
    }

    @Transactional
    public PlayerMeDto updateProfile(long userId, UpdatePlayerProfileRequest request, String gameType) {
        boolean hasNickname = request.getNickname() != null && !request.getNickname().isBlank();
        boolean hasAvatar = request.getAvatar() != null;
        if (!hasNickname && !hasAvatar) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请提供 nickname 或 avatar");
        }

        Player player = requirePlayer(userId);
        if (hasNickname) {
            player.setNickname(request.getNickname().trim());
        }
        if (hasAvatar) {
            player.setAvatar(request.getAvatar().trim());
        }
        player.setUpdatedAt(LocalDateTime.now());
        playerRepository.save(player);
        return getMe(userId, gameType);
    }

    private Player requirePlayer(long userId) {
        return playerRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYER_NOT_FOUND));
    }
}
