package io.github.kingqiang.cardgame.cardgamebackend.admin.service;

import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.OperationActions;
import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.service.OperationLogService;
import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.AdminUserListItemDto;
import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.BanUserRequest;
import io.github.kingqiang.cardgame.cardgamebackend.auth.security.SecurityUtils;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.RequestUtils;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageRequest;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageResult;
import io.github.kingqiang.cardgame.cardgamebackend.common.exception.BusinessException;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import io.github.kingqiang.cardgame.cardgamebackend.rank.entity.PlayerRank;
import io.github.kingqiang.cardgame.cardgamebackend.rank.enums.RankTier;
import io.github.kingqiang.cardgame.cardgamebackend.rank.service.RankService;
import io.github.kingqiang.cardgame.cardgamebackend.user.entity.Player;
import io.github.kingqiang.cardgame.cardgamebackend.user.entity.UserBan;
import io.github.kingqiang.cardgame.cardgamebackend.user.entity.UserWallet;
import io.github.kingqiang.cardgame.cardgamebackend.user.repository.PlayerRepository;
import io.github.kingqiang.cardgame.cardgamebackend.user.repository.UserBanRepository;
import io.github.kingqiang.cardgame.cardgamebackend.user.repository.UserWalletRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 业务服务：AdminUser 领域逻辑。
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final PlayerRepository playerRepository;
    private final UserWalletRepository userWalletRepository;
    private final UserBanRepository userBanRepository;
    private final OperationLogService operationLogService;
    private final RankService rankService;

    private static final String DEFAULT_GAME_TYPE = "DOUDIZHU";

    @Transactional(readOnly = true)
    public PageResult<AdminUserListItemDto> list(Long userId, String nickname, String status, PageRequest pageRequest) {
        Specification<Player> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null) {
                predicates.add(cb.equal(root.get("id"), userId));
            }
            if (nickname != null && !nickname.isBlank()) {
                predicates.add(cb.like(root.get("nickname"), "%" + nickname + "%"));
            }
            if ("BANNED".equalsIgnoreCase(status)) {
                predicates.add(cb.equal(root.get("status"), 1));
            } else if ("NORMAL".equalsIgnoreCase(status)) {
                predicates.add(cb.equal(root.get("status"), 0));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Player> page = playerRepository.findAll(spec,
                org.springframework.data.domain.PageRequest.of(pageRequest.getPage() - 1, pageRequest.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt")));
        List<Long> ids = page.getContent().stream().map(Player::getId).toList();
        Map<Long, UserWallet> wallets = userWalletRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(UserWallet::getUserId, w -> w));
        Map<Long, PlayerRank> ranks = rankService.findRanksForUsers(ids, DEFAULT_GAME_TYPE);
        List<AdminUserListItemDto> list = page.getContent().stream()
                .map(p -> {
                    PlayerRank rank = ranks.get(p.getId());
                    return AdminUserListItemDto.builder()
                            .id(p.getId())
                            .nickname(p.getNickname())
                            .avatar(p.getAvatar())
                            .openidMasked(maskOpenid(p.getOpenid()))
                            .gold(wallets.getOrDefault(p.getId(), new UserWallet()).getGold())
                            .statusLabel(p.getStatus() != null && p.getStatus() == 1 ? "封禁" : "正常")
                            .rankTier(rank != null ? rank.getTier() : RankTier.BRONZE.name())
                            .rankPoints(rank != null ? rank.getPoints() : 0)
                            .createdAt(p.getCreatedAt())
                            .build();
                })
                .toList();
        return PageResult.of(list, page.getTotalElements(), pageRequest.getPage(), pageRequest.getPageSize());
    }

    @Transactional
    public void banUser(Long id, BanUserRequest request) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYER_NOT_FOUND));
        player.setStatus(1);
        player.setUpdatedAt(LocalDateTime.now());
        playerRepository.save(player);

        UserBan ban = new UserBan();
        ban.setUserId(id);
        ban.setReason(request.getReason());
        ban.setBanUntil(request.getBanUntil());
        ban.setOperatorId(SecurityUtils.requireAdmin().getId());
        ban.setCreatedAt(LocalDateTime.now());
        userBanRepository.save(ban);

        operationLogService.log(
                SecurityUtils.requireAdmin().getId(),
                OperationActions.USER_BAN,
                "USER",
                String.valueOf(id),
                Map.of("reason", request.getReason(), "banUntil", request.getBanUntil()),
                RequestUtils.clientIp()
        );
    }

    @Transactional
    public void unbanUser(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYER_NOT_FOUND));
        player.setStatus(0);
        player.setUpdatedAt(LocalDateTime.now());
        playerRepository.save(player);
        userBanRepository.findActiveBan(id, LocalDateTime.now()).ifPresent(ban -> {
            ban.setRevokedAt(LocalDateTime.now());
            userBanRepository.save(ban);
        });

        operationLogService.log(
                SecurityUtils.requireAdmin().getId(),
                OperationActions.USER_UNBAN,
                "USER",
                String.valueOf(id),
                Map.of(),
                RequestUtils.clientIp()
        );
    }

    private String maskOpenid(String openid) {
        if (openid == null || openid.length() <= 8) {
            return openid;
        }
        return openid.substring(0, 4) + "***" + openid.substring(openid.length() - 4);
    }
}
