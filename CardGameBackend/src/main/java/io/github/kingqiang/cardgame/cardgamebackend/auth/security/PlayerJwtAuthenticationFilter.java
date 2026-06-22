package io.github.kingqiang.cardgame.cardgamebackend.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet 过滤器：PlayerJwtAuthentication。
 */
@Component
public class PlayerJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public PlayerJwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/v1/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = jwtService.parsePlayerToken(token);
            if (!JwtService.TYPE_PLAYER.equals(claims.get(JwtService.CLAIM_TYPE, String.class))) {
                filterChain.doFilter(request, response);
                return;
            }
            Long playerId = Long.parseLong(claims.getSubject());
            String nickname = claims.get("username", String.class);
            PlayerPrincipal principal = new PlayerPrincipal(playerId, nickname);
            SecurityContextHolder.getContext().setAuthentication(PlayerPrincipal.toAuthentication(principal));
        } catch (JwtException | IllegalArgumentException ignored) {
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }
}
