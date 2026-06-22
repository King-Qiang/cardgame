package io.github.kingqiang.cardgame.cardgamebackend.room.websocket;

import io.github.kingqiang.cardgame.cardgamebackend.auth.security.JwtService;
import io.github.kingqiang.cardgame.cardgamebackend.common.exception.BusinessException;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * 拦截器：GameWebSocketHandshake。
 */
@Component
public class GameWebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;

    public GameWebSocketHandshakeInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String query = request.getURI().getQuery();
        if (query == null || !query.contains("token=")) {
            return false;
        }
        String token = query.substring(query.indexOf("token=") + 6);
        int amp = token.indexOf('&');
        if (amp >= 0) {
            token = token.substring(0, amp);
        }
        try {
            Claims claims = jwtService.parsePlayerToken(token);
            if (!JwtService.TYPE_PLAYER.equals(claims.get(JwtService.CLAIM_TYPE, String.class))) {
                return false;
            }
            attributes.put("playerId", Long.parseLong(claims.getSubject()));
            attributes.put("nickname", claims.get("username", String.class));
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
