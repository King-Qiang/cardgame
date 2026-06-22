package io.github.kingqiang.cardgame.cardgamebackend.admin.service;

import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.OperationActions;
import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.service.OperationLogService;
import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.AdminAccountDto;
import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.CreateAdminUserRequest;
import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.ResetAdminPasswordRequest;
import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.UpdateAdminUserRequest;
import io.github.kingqiang.cardgame.cardgamebackend.admin.entity.AdminRole;
import io.github.kingqiang.cardgame.cardgamebackend.admin.entity.AdminUser;
import io.github.kingqiang.cardgame.cardgamebackend.admin.repository.AdminRoleRepository;
import io.github.kingqiang.cardgame.cardgamebackend.admin.repository.AdminUserRepository;
import io.github.kingqiang.cardgame.cardgamebackend.auth.security.SecurityUtils;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageRequest;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageResult;
import io.github.kingqiang.cardgame.cardgamebackend.common.exception.BusinessException;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.RequestUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 业务服务：AdminAccount 领域逻辑。
 */
@Service
@RequiredArgsConstructor
public class AdminAccountService {

    private final AdminUserRepository adminUserRepository;
    private final AdminRoleRepository adminRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final OperationLogService operationLogService;

    @Transactional(readOnly = true)
    public PageResult<AdminAccountDto> list(String username, Integer status, PageRequest pageRequest) {
        Specification<AdminUser> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (username != null && !username.isBlank()) {
                predicates.add(cb.like(root.get("username"), "%" + username + "%"));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<AdminUser> page = adminUserRepository.findAll(spec,
                org.springframework.data.domain.PageRequest.of(
                        pageRequest.getPage() - 1,
                        pageRequest.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")));
        List<Long> roleIds = page.getContent().stream().map(AdminUser::getRoleId).distinct().toList();
        Map<Long, AdminRole> roleMap = adminRoleRepository.findAllById(roleIds).stream()
                .collect(java.util.stream.Collectors.toMap(AdminRole::getId, r -> r));
        List<AdminAccountDto> list = page.getContent().stream()
                .map(user -> toDto(user, roleMap.get(user.getRoleId())))
                .toList();
        return PageResult.of(list, page.getTotalElements(), pageRequest.getPage(), pageRequest.getPageSize());
    }

    @Transactional
    public AdminAccountDto create(CreateAdminUserRequest request) {
        if (adminUserRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.ADMIN_USERNAME_EXISTS);
        }
        AdminRole role = adminRoleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));
        LocalDateTime now = LocalDateTime.now();
        AdminUser user = new AdminUser();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setRoleId(role.getId());
        user.setStatus(1);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        AdminUser saved = adminUserRepository.save(user);
        log(OperationActions.ADMIN_CREATE, saved.getId(), Map.of("username", saved.getUsername()));
        return toDto(saved, role);
    }

    @Transactional
    public AdminAccountDto update(Long id, UpdateAdminUserRequest request) {
        AdminUser user = findUser(id);
        long currentAdminId = SecurityUtils.requireAdmin().getId();
        if (currentAdminId == id && request.getStatus() != null && request.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不能禁用当前登录账号");
        }
        AdminRole role = adminRoleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));
        user.setRoleId(role.getId());
        user.setStatus(request.getStatus());
        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        user.setUpdatedAt(LocalDateTime.now());
        AdminUser saved = adminUserRepository.save(user);
        log(OperationActions.ADMIN_UPDATE, saved.getId(), Map.of("username", saved.getUsername()));
        return toDto(saved, role);
    }

    @Transactional
    public void resetPassword(Long id, ResetAdminPasswordRequest request) {
        AdminUser user = findUser(id);
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        adminUserRepository.save(user);
        log(OperationActions.ADMIN_RESET_PASSWORD, id, Map.of("username", user.getUsername()));
    }

    private AdminUser findUser(Long id) {
        return adminUserRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
    }

    private void log(String action, Long id, Map<String, Object> detail) {
        operationLogService.log(
                SecurityUtils.requireAdmin().getId(),
                action,
                "ADMIN",
                String.valueOf(id),
                detail,
                RequestUtils.clientIp()
        );
    }

    private AdminAccountDto toDto(AdminUser user, AdminRole role) {
        return AdminAccountDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .roleId(user.getRoleId())
                .roleName(role != null ? role.getName() : "—")
                .status(user.getStatus())
                .statusLabel(user.getStatus() != null && user.getStatus() == 1 ? "启用" : "禁用")
                .lastLoginAt(user.getLastLoginAt())
                .lastLoginIp(user.getLastLoginIp())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
