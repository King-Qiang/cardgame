package io.github.kingqiang.cardgame.cardgamebackend.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 业务错误码枚举，与 HTTP 状态及客户端文案映射。
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAM_ERROR(10001, "参数错误"),
    UNAUTHORIZED(10002, "未授权"),
    FORBIDDEN(10003, "无权限"),
    INTERNAL_ERROR(10004, "服务器内部错误"),
    ADMIN_NOT_FOUND(20001, "管理员不存在"),
    ADMIN_DISABLED(20002, "管理员已禁用"),
    INVALID_CREDENTIALS(20003, "用户名或密码错误"),
    PLAYER_NOT_FOUND(30001, "玩家不存在"),
    PLAYER_BANNED(30002, "账号已被封禁"),
    INVALID_REFRESH_TOKEN(30003, "无效的刷新令牌"),
    WECHAT_LOGIN_FAILED(30004, "微信登录失败"),
    ROOM_NOT_FOUND(40001, "房间不存在"),
    ROOM_FULL(40002, "房间已满"),
    ROOM_NOT_JOINABLE(40003, "当前无法加入房间"),
    NOT_IN_ROOM(40004, "不在该房间中"),
    MATCH_ALREADY_IN_QUEUE(40005, "已在匹配队列中"),
    MATCH_NOT_IN_QUEUE(40006, "未在匹配队列中"),
    PVE_DISABLED(40007, "人机练习未开启"),
    BOT_NOT_ALLOWED(40008, "当前房间不允许添加电脑"),
    GAME_ACTION_INVALID(50001, "非法游戏操作"),
    GAME_NOT_STARTED(50002, "对局尚未开始"),
    INSUFFICIENT_BALANCE(60001, "金币余额不足"),
    WALLET_UPDATE_CONFLICT(60002, "钱包更新冲突，请重试"),
    ADJUST_REQUIRES_APPROVAL(60003, "超过阈值，请提交调账审批"),
    ADJUST_REQUEST_NOT_FOUND(60004, "调账申请不存在"),
    ADJUST_REQUEST_INVALID_STATUS(60005, "调账申请状态不允许此操作"),
    SHOP_ITEM_NOT_FOUND(70001, "商品不存在"),
    ACTIVITY_NOT_FOUND(70002, "活动不存在"),
    ROLE_NOT_FOUND(70003, "角色不存在"),
    ROLE_IN_USE(70004, "角色仍有关联管理员，无法删除"),
    ADMIN_USERNAME_EXISTS(70005, "管理员用户名已存在"),
    ALREADY_SIGNED_TODAY(70006, "今日已签到"),
    ACTIVITY_DISABLED(70007, "活动未开启"),
    ORDER_NOT_FOUND(70008, "订单不存在"),
    ORDER_INVALID_STATUS(70009, "订单状态不允许此操作"),
    EXPORT_TOO_LARGE(90001, "导出数据量过大，请缩小筛选范围");

    private final int code;
    private final String message;
}
