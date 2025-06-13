package com.sessionflow.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 非同步配置
 * 
 * 配置非同步執行緒池，用於 WebSocket 事件處理
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    
    /**
     * WebSocket 事件處理執行緒池
     * 
     * @return 執行緒池執行器
     */
    @Bean(name = "websocketEventExecutor")
    public Executor websocketEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("websocket-event-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
} 