package io.github.kingqiang.cardgame.cardgamebackend.admin.audit.service;

import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.entity.OperationLog;
import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.repository.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 业务服务：OperationLog 领域逻辑。
 */
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogRepository operationLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long operatorId, String action, String targetType, String targetId,
                    Map<String, Object> detail, String ip) {
        OperationLog log = new OperationLog();
        log.setOperatorId(operatorId);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId != null ? targetId : "");
        log.setDetail(detail);
        log.setIp(ip != null ? ip : "");
        log.setCreatedAt(LocalDateTime.now());
        operationLogRepository.save(log);
    }
}
