package io.github.kingqiang.cardgame.cardgamebackend.economy.service;

import io.github.kingqiang.cardgame.cardgamebackend.common.exception.BusinessException;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.SystemConfigDto;
import io.github.kingqiang.cardgame.cardgamebackend.economy.dto.UpdateSystemConfigRequest;
import io.github.kingqiang.cardgame.cardgamebackend.economy.entity.SystemConfig;
import io.github.kingqiang.cardgame.cardgamebackend.economy.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 业务服务：SystemConfig 领域逻辑。
 */
@Service
@RequiredArgsConstructor
public class SystemConfigService {

    public static final String ADJUST_THRESHOLD_KEY = "wallet.adjust_threshold";

    private final SystemConfigRepository systemConfigRepository;

    @Transactional(readOnly = true)
    public List<SystemConfigDto> listAll() {
        return systemConfigRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SystemConfigDto getByKey(String key) {
        return toDto(findConfig(key));
    }

    @Transactional
    public SystemConfigDto update(String key, UpdateSystemConfigRequest request) {
        SystemConfig config = findConfig(key);
        config.setConfigValue(request.getConfigValue());
        config.setUpdatedAt(LocalDateTime.now());
        return toDto(systemConfigRepository.save(config));
    }

    @Transactional(readOnly = true)
    public long getAdjustThresholdAmount() {
        return readThresholdConfig().thresholdAmount();
    }

    @Transactional(readOnly = true)
    public boolean isAdjustApprovalRequired(long amount) {
        ThresholdConfig thresholdConfig = readThresholdConfig();
        return thresholdConfig.requireApproval() && amount >= thresholdConfig.thresholdAmount();
    }

    private ThresholdConfig readThresholdConfig() {
        SystemConfig config = systemConfigRepository.findById(ADJUST_THRESHOLD_KEY).orElse(null);
        if (config == null || !(config.getConfigValue() instanceof Map<?, ?> value)) {
            return new ThresholdConfig(100_000L, true);
        }
        Object threshold = value.get("amount");
        long thresholdAmount = threshold instanceof Number n ? n.longValue() : 100_000L;
        Object requireApproval = value.get("require_approval");
        boolean needApproval = requireApproval instanceof Boolean b ? b : true;
        return new ThresholdConfig(thresholdAmount, needApproval);
    }

    private record ThresholdConfig(long thresholdAmount, boolean requireApproval) {
    }

    private SystemConfig findConfig(String key) {
        return systemConfigRepository.findById(key)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR, "配置不存在: " + key));
    }

    private SystemConfigDto toDto(SystemConfig config) {
        return SystemConfigDto.builder()
                .configKey(config.getConfigKey())
                .configValue(config.getConfigValue())
                .description(config.getDescription())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
