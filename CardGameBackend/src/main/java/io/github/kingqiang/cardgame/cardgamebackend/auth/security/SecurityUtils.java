package io.github.kingqiang.cardgame.cardgamebackend.auth.security;

import io.github.kingqiang.cardgame.cardgamebackend.common.exception.BusinessException;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 工具类：SecurityUtils。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static long requirePlayerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof PlayerPrincipal principal) {
            return principal.getPlayerId();
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }

    public static AdminPrincipal requireAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AdminPrincipal principal) {
            return principal;
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
}
