package cn.colin.ws;

import cn.colin.utils.JwtUtil;
import cn.colin.utils.TokenUtil;
import com.auth0.jwt.exceptions.TokenExpiredException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 */
@Slf4j
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private static final Map<String, WebSocketSession> SESSION_MAP = new ConcurrentHashMap<>();

    private static ScheduledThreadPoolExecutor validTokenExecutor;

    @Resource
    public void setValidTokenExecutor(ScheduledThreadPoolExecutor validTokenExecutor) {
        NotificationWebSocketHandler.validTokenExecutor = validTokenExecutor;
    }

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
        log.info("WebSocket 连接建立");
        String token = getToken(session);
        if (token == null) {
            session.close();
            return;
        }
        SESSION_MAP.put(token, session);
        // 建立定时器校验token是否过期
        ScheduledFuture<?> scheduledFuture = validTokenExecutor.scheduleAtFixedRate(() -> {
            try {
                JwtUtil.verify(token);
            } catch (TokenExpiredException e) {
                sendExpireMessage(token);
            }
        }, 0, 60, TimeUnit.SECONDS);
        session.getAttributes().put("scheduledFuture", scheduledFuture);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NotNull CloseStatus status) throws Exception {
        log.info("WebSocket 连接断开");
        ScheduledFuture<?> scheduledFuture = (ScheduledFuture<?>) session.getAttributes().get("scheduledFuture");
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
        String token = getToken(session);
        if (StringUtils.isNotEmpty(token)) {
            SESSION_MAP.remove(token);
        }
        session.close();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        session.sendMessage(new TextMessage(message.getPayload()));
    }

    public static void sendMessage(String message) {
        for (WebSocketSession session : SESSION_MAP.values()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("WebSocket 消息发送失败", e);
            }
        }
    }

    public static void sendMessage(String userName, String message) {
        String token = TokenUtil.getToken(userName);
        WebSocketSession session = SESSION_MAP.get(token);
        if (session == null) {
            return;
        }
        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            log.error("WebSocket 消息发送失败", e);
        }
    }

    public static void sendExpireMessage(String token) {
        if (StringUtils.isEmpty(token) || SESSION_MAP.get(token) == null) {
            return;
        }
        try {
            WebSocketSession session = SESSION_MAP.get(token);
            session.sendMessage(new TextMessage("你的登录已过期，请重新登录"));
            SESSION_MAP.remove(token);
            session.close();
        } catch (IOException e) {
            log.error("WebSocket 过期通知发送失败", e);
        }
    }

    private String getToken(WebSocketSession session) {
        List<String> tokenList = session.getHandshakeHeaders().get("token");
        if (CollectionUtils.isEmpty(tokenList)) {
            return null;
        }
        return tokenList.getFirst();
    }
}
