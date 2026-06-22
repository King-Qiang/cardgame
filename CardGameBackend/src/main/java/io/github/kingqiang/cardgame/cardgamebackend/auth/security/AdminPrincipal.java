package io.github.kingqiang.cardgame.cardgamebackend.auth.security;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

/**
 * AdminPrincipal。
 */
@Getter
public class AdminPrincipal {

    private final Long id;
    private final String username;
    private final List<SimpleGrantedAuthority> authorities;

    public AdminPrincipal(Long id, String username, List<String> permissions) {
        this.id = id;
        this.username = username;
        this.authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
