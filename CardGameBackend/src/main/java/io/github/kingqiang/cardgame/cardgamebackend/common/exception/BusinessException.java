package io.github.kingqiang.cardgame.cardgamebackend.common.exception;

import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import lombok.Getter;

/**
 * 异常类型：Business。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
