package io.github.kingqiang.cardgame.cardgamebackend.auth.wechat;

/**
 * WechatSession。
 */
public record WechatSession(String openid, String sessionKey, String unionid) {
}
