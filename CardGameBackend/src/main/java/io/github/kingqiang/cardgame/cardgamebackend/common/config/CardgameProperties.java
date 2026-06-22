package io.github.kingqiang.cardgame.cardgamebackend.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置属性绑定：Cardgame。
 */
@ConfigurationProperties(prefix = "cardgame")
public record CardgameProperties(
        JwtProperties jwt,
        WechatProperties wechat,
        RedisProperties redis,
        JobProperties job,
        SettlementProperties settlement
) {

    public record JwtProperties(
            String playerSecret,
            String adminSecret,
            long accessExpire,
            long refreshExpire
    ) {
    }

    public record WechatProperties(
            String appId,
            String appSecret,
            boolean mockEnabled
    ) {
        public boolean isConfigured() {
            return appId != null && !appId.isBlank()
                    && appSecret != null && !appSecret.isBlank();
        }
    }

    public record RedisProperties(
            boolean enabled,
            String wsChannelPrefix
    ) {
        public RedisProperties {
            if (wsChannelPrefix == null || wsChannelPrefix.isBlank()) {
                wsChannelPrefix = "ws:room:";
            }
        }
    }

    public record JobProperties(
            long staleRoomHours
    ) {
        public JobProperties {
            if (staleRoomHours <= 0) {
                staleRoomHours = 2;
            }
        }
    }

    public record SettlementProperties(
            boolean asyncEnabled,
            String streamName,
            String consumerGroup,
            String consumerName
    ) {
        public SettlementProperties {
            if (streamName == null || streamName.isBlank()) {
                streamName = "cardgame:settlement";
            }
            if (consumerGroup == null || consumerGroup.isBlank()) {
                consumerGroup = "settlement-workers";
            }
            if (consumerName == null || consumerName.isBlank()) {
                consumerName = "worker-1";
            }
        }
    }
}
