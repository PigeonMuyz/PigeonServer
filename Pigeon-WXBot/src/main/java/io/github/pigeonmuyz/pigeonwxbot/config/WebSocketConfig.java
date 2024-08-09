package io.github.pigeonmuyz.pigeonwxbot.config;

import io.github.pigeonmuyz.pigeonwxbot.handler.WeChatHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket服务端 配置
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WeChatHandler weChatHandler;

    @Autowired
    public WebSocketConfig(WeChatHandler weChatHandler) {
        this.weChatHandler = weChatHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(weChatHandler, "/wechat").setAllowedOrigins("*");
    }
}
