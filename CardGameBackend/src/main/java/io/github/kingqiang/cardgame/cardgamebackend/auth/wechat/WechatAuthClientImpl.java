package io.github.kingqiang.cardgame.cardgamebackend.auth.wechat;

import io.github.kingqiang.cardgame.cardgamebackend.common.config.CardgameProperties;
import io.github.kingqiang.cardgame.cardgamebackend.common.exception.BusinessException;
import io.github.kingqiang.cardgame.cardgamebackend.common.response.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

/**
 * WechatAuthClientImpl。
 */
@Component
public class WechatAuthClientImpl implements WechatAuthClient {

    private final CardgameProperties properties;
    private final RestClient restClient = RestClient.create();

    public WechatAuthClientImpl(CardgameProperties properties) {
        this.properties = properties;
    }

    @Override
    public WechatSession code2Session(String code) {
        if (properties.wechat().isConfigured()) {
            return callWechatApi(code);
        }
        if (properties.wechat().mockEnabled()) {
            return mockSession(code);
        }
        throw new BusinessException(ErrorCode.WECHAT_LOGIN_FAILED, "未配置微信 AppId，且 mock 已关闭");
    }

    @SuppressWarnings("unchecked")
    private WechatSession callWechatApi(String code) {
        Map<String, Object> body = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.weixin.qq.com")
                        .path("/sns/jscode2session")
                        .queryParam("appid", properties.wechat().appId())
                        .queryParam("secret", properties.wechat().appSecret())
                        .queryParam("js_code", code)
                        .queryParam("grant_type", "authorization_code")
                        .build())
                .retrieve()
                .body(Map.class);
        if (body == null || body.get("openid") == null) {
            throw new BusinessException(ErrorCode.WECHAT_LOGIN_FAILED);
        }
        return new WechatSession(
                String.valueOf(body.get("openid")),
                body.get("session_key") != null ? String.valueOf(body.get("session_key")) : "",
                body.get("unionid") != null ? String.valueOf(body.get("unionid")) : null
        );
    }

    private WechatSession mockSession(String code) {
        String openid = "dev_" + sha256(code).substring(0, 16);
        return new WechatSession(openid, "mock_session_key", null);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
