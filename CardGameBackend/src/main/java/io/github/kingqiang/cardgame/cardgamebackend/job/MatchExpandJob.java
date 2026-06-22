package io.github.kingqiang.cardgame.cardgamebackend.job;

import io.github.kingqiang.cardgame.cardgamebackend.match.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务：MatchExpand。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchExpandJob {

    private final MatchService matchService;

    @Scheduled(fixedDelayString = "${cardgame.job.match-expand-interval-ms:10000}")
    public void expandRankedMatches() {
        int matched = matchService.tryExpandRankedMatches();
        if (matched > 0) {
            log.info("Ranked match expand job created {} rooms", matched);
        }
    }
}
