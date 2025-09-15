package com.example.siteback.Config;

import com.example.siteback.Handler.BaChatWebSocketHandler;

import org.springframework.context.annotation.Configuration;

import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSocket
public class BaWebSocketConfig implements WebSocketConfigurer {

    private final BaChatWebSocketHandler chatHandler;

    // 构造器注入你的WebSocket处理器
    public BaWebSocketConfig(BaChatWebSocketHandler chatHandler) {
        this.chatHandler = chatHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler, "/ws/chat")
                .setAllowedOrigins("*");  // 根据需要设置允许跨域的域名，* 表示全部允许
    }

}
