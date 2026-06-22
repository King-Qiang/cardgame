package io.github.kingqiang.cardgame.cardgamebackend.common.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ID 生成器：BusinessId。
 */
@Component
public class BusinessIdGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final AtomicInteger sequence = new AtomicInteger(0);
    private volatile String lastSecond = "";

    public String nextRoomId() {
        return "R" + nextId();
    }

    public String nextRecordId() {
        return "GR" + nextId();
    }

    public String nextOrderNo() {
        return "O" + nextId();
    }

    private String nextId() {
        String second = LocalDateTime.now().format(FORMATTER);
        synchronized (this) {
            if (!second.equals(lastSecond)) {
                lastSecond = second;
                sequence.set(0);
            }
            int seq = sequence.incrementAndGet();
            if (seq > 999) {
                seq = seq % 1000;
            }
            return second + String.format("%03d", seq);
        }
    }
}
