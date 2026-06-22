package io.github.kingqiang.cardgame.cardgamebackend.auth.security;

import io.github.kingqiang.cardgame.cardgamebackend.common.config.CardgameProperties;
import io.github.kingqiang.cardgame.cardgamebackend.common.exception.BusinessException;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 业务服务：Jwt 领域逻辑。
 */
@Component
public class JwtService {

    public static final String CLAIM_TYPE = "type";
    public static final String CLAIM_PERMISSIONS = "permissions";
    public static final String TYPE_ADMIN = "admin";
    public static final String TYPE_ADMIN_REFRESH = "admin_refresh";
    public static final String TYPE_PLAYER = "player";
    public static final String TYPE_PLAYER_REFRESH = "player_refresh";

    private final CardgameProperties properties;

    public JwtService(CardgameProperties properties) {
        this.properties = properties;
    }

    public String generateAdminAccessToken(Long adminId, String username, List<String> permissions) {
        return buildToken(adminId, username, properties.jwt().adminSecret(), properties.jwt().accessExpire(),
                Map.of(CLAIM_TYPE, TYPE_ADMIN, CLAIM_PERMISSIONS, permissions));
    }

    public String generateAdminRefreshToken(Long adminId, String username) {
        return buildToken(adminId, username, properties.jwt().adminSecret(), properties.jwt().refreshExpire(),
                Map.of(CLAIM_TYPE, TYPE_ADMIN_REFRESH));
    }

    public String generatePlayerAccessToken(Long playerId, String nickname) {
        return buildToken(playerId, nickname, properties.jwt().playerSecret(), properties.jwt().accessExpire(),
                Map.of(CLAIM_TYPE, TYPE_PLAYER));
    }

    public String generatePlayerRefreshToken(Long playerId, String nickname) {
        return buildToken(playerId, nickname, properties.jwt().playerSecret(), properties.jwt().refreshExpire(),
                Map.of(CLAIM_TYPE, TYPE_PLAYER_REFRESH));
    }

    public Claims parseAdminToken(String token) {
        return parseToken(token, properties.jwt().adminSecret());
    }

    public Claims parsePlayerToken(String token) {
        return parseToken(token, properties.jwt().playerSecret());
    }

    public Claims parseAdminRefreshToken(String refreshToken) {
        Claims claims = parseAdminToken(refreshToken);
        if (!TYPE_ADMIN_REFRESH.equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        return claims;
    }

    public Claims parsePlayerRefreshToken(String refreshToken) {
        Claims claims = parsePlayerToken(refreshToken);
        if (!TYPE_PLAYER_REFRESH.equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        return claims;
    }

    private Claims parseToken(String token, String secret) {
        return Jwts.parser()
                .verifyWith(resolveKey(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String buildToken(Long subjectId, String username, String secret, long expireSeconds, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(subjectId))
                .claim("username", username)
                .claims(extraClaims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expireSeconds)))
                .signWith(resolveKey(secret))
                .compact();
    }

    private SecretKey resolveKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
