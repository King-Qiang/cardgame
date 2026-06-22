package io.github.kingqiang.cardgame.cardgamebackend.auth.wechat;

/**
 * WechatAuthClient。
 */
public interface WechatAuthClient {

    WechatSession code2Session(String code);
}
