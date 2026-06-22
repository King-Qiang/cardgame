package io.github.kingqiang.cardgame.cardgamebackend.auth.security;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;

/**
 * PlayerPrincipal。
 */
@Getter
public class PlayerPrincipal {

    private final Long playerId;
    private final String nickname;

    public PlayerPrincipal(Long playerId, String nickname) {
        this.playerId = playerId;
        this.nickname = nickname;
    }

    public static Authentication toAuthentication(PlayerPrincipal principal) {
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_PLAYER"));
        return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                principal, null, authorities);
    }
}
