package io.github.kingqiang.cardgame.cardgamebackend.admin.audit.repository;

import io.github.kingqiang.cardgame.cardgamebackend.admin.audit.entity.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * JPA 仓储：OperationLog 数据访问。
 */
public interface OperationLogRepository extends JpaRepository<OperationLog, Long>, JpaSpecificationExecutor<OperationLog> {
}
