package io.github.kingqiang.cardgame.cardgamebackend.admin.service;

import io.github.kingqiang.cardgame.cardgamebackend.auth.dto.TokenPairResponse;
import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.AdminLoginRequest;
import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.AdminLoginResponse;
import io.github.kingqiang.cardgame.cardgamebackend.admin.entity.AdminRole;
import io.github.kingqiang.cardgame.cardgamebackend.admin.entity.AdminUser;
import io.github.kingqiang.cardgame.cardgamebackend.admin.repository.AdminUserRepository;
import io.github.kingqiang.cardgame.cardgamebackend.auth.security.JwtService;
import io.github.kingqiang.cardgame.cardgamebackend.common.config.CardgameProperties;
import io.github.kingqiang.cardgame.cardgamebackend.common.exception.BusinessException;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.jsonwebtoken.Claims;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 业务服务：AdminAuth 领域逻辑。
 */
@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CardgameProperties cardgameProperties;

    @Transactional
    public AdminLoginResponse login(AdminLoginRequest request, String clientIp) {
        AdminUser admin = adminUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (admin.getStatus() == null || admin.getStatus() != 1) {
            throw new BusinessException(ErrorCode.ADMIN_DISABLED);
        }

        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        AdminRole role = admin.getRole();
        List<String> permissions = role != null && role.getPermissions() != null
                ? role.getPermissions()
                : Collections.emptyList();

        admin.setLastLoginAt(LocalDateTime.now());
        admin.setLastLoginIp(clientIp);
        adminUserRepository.save(admin);

        String accessToken = jwtService.generateAdminAccessToken(admin.getId(), admin.getUsername(), permissions);
        String refreshToken = jwtService.generateAdminRefreshToken(admin.getId(), admin.getUsername());

        return AdminLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(cardgameProperties.jwt().accessExpire())
                .user(AdminLoginResponse.AdminUserDto.builder()
                        .id(admin.getId())
                        .username(admin.getUsername())
                        .realName(admin.getRealName())
                        .build())
                .permissions(permissions)
                .build();
    }

    public TokenPairResponse refresh(String refreshToken) {
        Claims claims = jwtService.parseAdminRefreshToken(refreshToken);
        Long adminId = Long.parseLong(claims.getSubject());
        AdminUser admin = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
        if (admin.getStatus() == null || admin.getStatus() != 1) {
            throw new BusinessException(ErrorCode.ADMIN_DISABLED);
        }
        AdminRole role = admin.getRole();
        List<String> permissions = role != null && role.getPermissions() != null
                ? role.getPermissions()
                : Collections.emptyList();
        String accessToken = jwtService.generateAdminAccessToken(admin.getId(), admin.getUsername(), permissions);
        String newRefreshToken = jwtService.generateAdminRefreshToken(admin.getId(), admin.getUsername());
        return TokenPairResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(cardgameProperties.jwt().accessExpire())
                .build();
    }
}
