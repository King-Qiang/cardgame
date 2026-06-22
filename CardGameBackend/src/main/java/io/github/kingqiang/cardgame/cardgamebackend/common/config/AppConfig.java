package io.github.kingqiang.cardgame.cardgamebackend.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Spring 配置类：App。
 */
@Configuration
@EnableConfigurationProperties(CardgameProperties.class)
public class AppConfig {
}
