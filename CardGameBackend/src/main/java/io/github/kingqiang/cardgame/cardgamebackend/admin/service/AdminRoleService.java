package io.github.kingqiang.cardgame.cardgamebackend.admin.service;

import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.OperationActions;
import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.service.OperationLogService;
import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.AdminRoleDto;
import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.AdminRoleUpsertRequest;
import io.github.kingqiang.cardgame.cardgamebackend.admin.entity.AdminRole;
import io.github.kingqiang.cardgame.cardgamebackend.admin.repository.AdminRoleRepository;
import io.github.kingqiang.cardgame.cardgamebackend.admin.repository.AdminUserRepository;
import io.github.kingqiang.cardgame.cardgamebackend.auth.security.SecurityUtils;
import io.github.kingqiang.cardgame.cardgamebackend.common.exception.BusinessException;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import io.github.kingqiang.cardgame.cardgamebackend.common.util.RequestUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 业务服务：AdminRole 领域逻辑。
 */
@Service
@RequiredArgsConstructor
public class AdminRoleService {

    private final AdminRoleRepository adminRoleRepository;
    private final AdminUserRepository adminUserRepository;
    private final OperationLogService operationLogService;

    @Transactional(readOnly = true)
    public List<AdminRoleDto> list() {
        return adminRoleRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public AdminRoleDto get(Long id) {
        return toDto(findRole(id));
    }

    @Transactional
    public AdminRoleDto create(AdminRoleUpsertRequest request) {
        if (adminRoleRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "角色名已存在");
        }
        LocalDateTime now = LocalDateTime.now();
        AdminRole role = new AdminRole();
        role.setName(request.getName());
        role.setDescription(request.getDescription() != null ? request.getDescription() : "");
        role.setPermissions(request.getPermissions());
        role.setCreatedAt(now);
        role.setUpdatedAt(now);
        AdminRole saved = adminRoleRepository.save(role);
        log(OperationActions.ROLE_CREATE, saved.getId(), Map.of("name", saved.getName()));
        return toDto(saved);
    }

    @Transactional
    public AdminRoleDto update(Long id, AdminRoleUpsertRequest request) {
        AdminRole role = findRole(id);
        if (!role.getName().equals(request.getName()) && adminRoleRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "角色名已存在");
        }
        role.setName(request.getName());
        role.setDescription(request.getDescription() != null ? request.getDescription() : "");
        role.setPermissions(request.getPermissions());
        role.setUpdatedAt(LocalDateTime.now());
        AdminRole saved = adminRoleRepository.save(role);
        log(OperationActions.ROLE_UPDATE, saved.getId(), Map.of("name", saved.getName()));
        return toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (id == 1L) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不能删除超级管理员角色");
        }
        if (adminUserRepository.countByRoleId(id) > 0) {
            throw new BusinessException(ErrorCode.ROLE_IN_USE);
        }
        AdminRole role = findRole(id);
        adminRoleRepository.delete(role);
        log(OperationActions.ROLE_DELETE, id, Map.of("name", role.getName()));
    }

    private AdminRole findRole(Long id) {
        return adminRoleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));
    }

    private void log(String action, Long id, Map<String, Object> detail) {
        operationLogService.log(
                SecurityUtils.requireAdmin().getId(),
                action,
                "ROLE",
                String.valueOf(id),
                detail,
                RequestUtils.clientIp()
        );
    }

    private AdminRoleDto toDto(AdminRole role) {
        List<String> permissions = role.getPermissions() != null ? role.getPermissions() : List.of();
        return AdminRoleDto.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(permissions)
                .permissionCount(permissions.size())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}
