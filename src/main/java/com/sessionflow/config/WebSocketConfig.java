package com.sessionflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.lang.NonNull;

/**
 * WebSocket 配置類別
 * 
 * 配置 STOMP 協議的 WebSocket 連接，用於即時資料同步
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置訊息代理
     * 
     * @param config 訊息代理註冊器
     */
    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        // 啟用簡單的記憶體訊息代理，處理 "/topic" 前綴的訊息
        config.enableSimpleBroker("/topic");
        
        // 設定應用程式目的地前綴，客戶端發送訊息時使用
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * 註冊 STOMP 端點
     * 
     * @param registry STOMP 端點註冊器
     */
    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // 註冊 "/ws" 端點，啟用 SockJS 支援
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // 允許所有來源 (開發環境)
                .withSockJS(); // 啟用 SockJS 回退選項
    }
} 