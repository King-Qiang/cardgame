package io.github.kingqiang.cardgame.cardgamebackend.admin.service;

import io.github.kingqiang.cardgame.cardgamebackend.activity.dto.ActivityDto;
import io.github.kingqiang.cardgame.cardgamebackend.activity.dto.ActivityUpsertRequest;
import io.github.kingqiang.cardgame.cardgamebackend.activity.entity.ActivityConfig;
import io.github.kingqiang.cardgame.cardgamebackend.activity.repository.ActivityConfigRepository;
import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.OperationActions;
import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.service.OperationLogService;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 业务服务：AdminActivity 领域逻辑。
 */
@Service
@RequiredArgsConstructor
public class AdminActivityService {

    private final ActivityConfigRepository activityConfigRepository;
    private final OperationLogService operationLogService;

    @Transactional(readOnly = true)
    public PageResult<ActivityDto> list(Integer status, String type, PageRequest pageRequest) {
        Specification<ActivityConfig> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (type != null && !type.isBlank()) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<ActivityConfig> page = activityConfigRepository.findAll(spec,
                org.springframework.data.domain.PageRequest.of(
                        pageRequest.getPage() - 1,
                        pageRequest.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "updatedAt")));
        List<ActivityDto> list = page.getContent().stream().map(this::toDto).toList();
        return PageResult.of(list, page.getTotalElements(), pageRequest.getPage(), pageRequest.getPageSize());
    }

    @Transactional(readOnly = true)
    public ActivityDto get(Long id) {
        return toDto(findActivity(id));
    }

    @Transactional
    public ActivityDto create(ActivityUpsertRequest request) {
        if (activityConfigRepository.existsByCode(request.getCode())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "活动编码已存在");
        }
        LocalDateTime now = LocalDateTime.now();
        ActivityConfig entity = new ActivityConfig();
        applyRequest(entity, request);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        ActivityConfig saved = activityConfigRepository.save(entity);
        log(OperationActions.ACTIVITY_CREATE, saved.getId(), Map.of("code", saved.getCode()));
        return toDto(saved);
    }

    @Transactional
    public ActivityDto update(Long id, ActivityUpsertRequest request) {
        ActivityConfig entity = findActivity(id);
        if (!entity.getCode().equals(request.getCode())
                && activityConfigRepository.existsByCode(request.getCode())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "活动编码已存在");
        }
        applyRequest(entity, request);
        entity.setUpdatedAt(LocalDateTime.now());
        ActivityConfig saved = activityConfigRepository.save(entity);
        log(OperationActions.ACTIVITY_UPDATE, saved.getId(), Map.of("code", saved.getCode()));
        return toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        ActivityConfig entity = findActivity(id);
        activityConfigRepository.delete(entity);
        log(OperationActions.ACTIVITY_DELETE, id, Map.of("code", entity.getCode()));
    }

    private ActivityConfig findActivity(Long id) {
        return activityConfigRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND));
    }

    private void applyRequest(ActivityConfig entity, ActivityUpsertRequest request) {
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setType(request.getType());
        entity.setConfigJson(request.getConfigJson());
        entity.setStatus(request.getStatus());
        entity.setStartAt(request.getStartAt());
        entity.setEndAt(request.getEndAt());
    }

    private void log(String action, Long id, Map<String, Object> detail) {
        operationLogService.log(
                SecurityUtils.requireAdmin().getId(),
                action,
                "ACTIVITY",
                String.valueOf(id),
                detail,
                RequestUtils.clientIp()
        );
    }

    private ActivityDto toDto(ActivityConfig entity) {
        return ActivityDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .type(entity.getType())
                .configJson(entity.getConfigJson())
                .status(entity.getStatus())
                .statusLabel(entity.getStatus() != null && entity.getStatus() == 1 ? "启用" : "禁用")
                .startAt(entity.getStartAt())
                .endAt(entity.getEndAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
