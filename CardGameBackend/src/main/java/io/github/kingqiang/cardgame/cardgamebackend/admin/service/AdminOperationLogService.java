package io.github.kingqiang.cardgame.cardgamebackend.admin.service;

import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.entity.OperationLog;
import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.repository.OperationLogRepository;
import io.github.kingqiang.cardgame.cardgamebackend.admin.dto.OperationLogDto;
import io.github.kingqiang.cardgame.cardgamebackend.admin.entity.AdminUser;
import io.github.kingqiang.cardgame.cardgamebackend.admin.repository.AdminUserRepository;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageRequest;
import io.github.kingqiang.cardgame.cardgamebackend.common.dto.PageResult;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 业务服务：AdminOperationLog 领域逻辑。
 */
@Service
@RequiredArgsConstructor
public class AdminOperationLogService {

    private final OperationLogRepository operationLogRepository;
    private final AdminUserRepository adminUserRepository;

    @Transactional(readOnly = true)
    public PageResult<OperationLogDto> list(Long operatorId, String action, String targetType, PageRequest pageRequest) {
        Specification<OperationLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (operatorId != null) {
                predicates.add(cb.equal(root.get("operatorId"), operatorId));
            }
            if (action != null && !action.isBlank()) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (targetType != null && !targetType.isBlank()) {
                predicates.add(cb.equal(root.get("targetType"), targetType));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<OperationLog> page = operationLogRepository.findAll(spec,
                org.springframework.data.domain.PageRequest.of(
                        pageRequest.getPage() - 1,
                        pageRequest.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")));
        List<Long> operatorIds = page.getContent().stream().map(OperationLog::getOperatorId).distinct().toList();
        Map<Long, String> operatorNames = adminUserRepository.findAllById(operatorIds).stream()
                .collect(Collectors.toMap(AdminUser::getId, AdminUser::getUsername));
        List<OperationLogDto> list = page.getContent().stream()
                .map(log -> toDto(log, operatorNames.getOrDefault(log.getOperatorId(), "—")))
                .toList();
        return PageResult.of(list, page.getTotalElements(), pageRequest.getPage(), pageRequest.getPageSize());
    }

    private OperationLogDto toDto(OperationLog log, String operatorName) {
        return OperationLogDto.builder()
                .id(log.getId())
                .operatorId(log.getOperatorId())
                .operatorName(operatorName)
                .action(log.getAction())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .detail(log.getDetail())
                .ip(log.getIp())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
