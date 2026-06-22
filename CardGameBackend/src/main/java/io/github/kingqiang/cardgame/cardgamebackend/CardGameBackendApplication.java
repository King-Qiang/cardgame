package io.github.kingqiang.cardgame.cardgamebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot 应用入口；启用定时任务与全模块组件扫描。
 */
@SpringBootApplication
@EnableScheduling
public class CardGameBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardGameBackendApplication.class, args);
    }

}
