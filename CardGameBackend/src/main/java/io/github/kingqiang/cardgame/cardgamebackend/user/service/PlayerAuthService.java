package io.github.kingqiang.cardgame.cardgamebackend.user.service;

import io.github.kingqiang.cardgame.cardgamebackend.auth.dto.TokenPairResponse;
import io.github.kingqiang.cardgame.cardgamebackend.auth.security.JwtService;
import io.github.kingqiang.cardgame.cardgamebackend.auth.wechat.WechatAuthClient;
import io.github.kingqiang.cardgame.cardgamebackend.auth.wechat.WechatSession;
import io.github.kingqiang.cardgame.cardgamebackend.common.config.CardgameProperties;
import io.github.kingqiang.cardgame.cardgamebackend.common.exception.BusinessException;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import io.github.kingqiang.cardgame.cardgamebackend.user.dto.PlayerLoginResponse;
import io.github.kingqiang.cardgame.cardgamebackend.user.entity.Player;
import io.github.kingqiang.cardgame.cardgamebackend.user.entity.UserProfile;
import io.github.kingqiang.cardgame.cardgamebackend.user.entity.UserWallet;
import io.github.kingqiang.cardgame.cardgamebackend.user.repository.PlayerRepository;
import io.github.kingqiang.cardgame.cardgamebackend.user.repository.UserBanRepository;
import io.github.kingqiang.cardgame.cardgamebackend.user.repository.UserProfileRepository;
import io.github.kingqiang.cardgame.cardgamebackend.user.repository.UserWalletRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 业务服务：PlayerAuth 领域逻辑。
 */
@Service
@RequiredArgsConstructor
public class PlayerAuthService {

    private final WechatAuthClient wechatAuthClient;
    private final PlayerRepository playerRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserWalletRepository userWalletRepository;
    private final UserBanRepository userBanRepository;
    private final JwtService jwtService;
    private final CardgameProperties cardgameProperties;

    @Transactional
    public PlayerLoginResponse loginByWechat(String code) {
        WechatSession session = wechatAuthClient.code2Session(code);
        Player player = playerRepository.findByOpenid(session.openid())
                .orElseGet(() -> createPlayer(session));

        ensurePlayerActive(player);

        String accessToken = jwtService.generatePlayerAccessToken(player.getId(), player.getNickname());
        String refreshToken = jwtService.generatePlayerRefreshToken(player.getId(), player.getNickname());

        return PlayerLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(cardgameProperties.jwt().accessExpire())
                .user(PlayerLoginResponse.PlayerUserDto.builder()
                        .id(player.getId())
                        .nickname(player.getNickname())
                        .avatar(player.getAvatar())
                        .build())
                .build();
    }

    public TokenPairResponse refresh(String refreshToken) {
        Claims claims = jwtService.parsePlayerRefreshToken(refreshToken);
        Long playerId = Long.parseLong(claims.getSubject());
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYER_NOT_FOUND));
        ensurePlayerActive(player);

        String accessToken = jwtService.generatePlayerAccessToken(player.getId(), player.getNickname());
        String newRefreshToken = jwtService.generatePlayerRefreshToken(player.getId(), player.getNickname());
        return TokenPairResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(cardgameProperties.jwt().accessExpire())
                .build();
    }

    private Player createPlayer(WechatSession session) {
        LocalDateTime now = LocalDateTime.now();
        Player player = new Player();
        player.setOpenid(session.openid());
        player.setUnionid(session.unionid());
        player.setNickname("玩家" + session.openid().substring(Math.max(0, session.openid().length() - 4)));
        player.setAvatar("");
        player.setStatus(0);
        player.setCreatedAt(now);
        player.setUpdatedAt(now);
        player = playerRepository.save(player);

        UserProfile profile = new UserProfile();
        profile.setUserId(player.getId());
        profile.setCreatedAt(now);
        profile.setUpdatedAt(now);
        userProfileRepository.save(profile);

        UserWallet wallet = new UserWallet();
        wallet.setUserId(player.getId());
        wallet.setGold(1000L);
        wallet.setCreatedAt(now);
        wallet.setUpdatedAt(now);
        userWalletRepository.save(wallet);

        return player;
    }

    private void ensurePlayerActive(Player player) {
        if (player.getStatus() != null && player.getStatus() == 1) {
            throw new BusinessException(ErrorCode.PLAYER_BANNED);
        }
        userBanRepository.findActiveBan(player.getId(), LocalDateTime.now())
                .ifPresent(ban -> {
                    throw new BusinessException(ErrorCode.PLAYER_BANNED);
                });
    }
}
