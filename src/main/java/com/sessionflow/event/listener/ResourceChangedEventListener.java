package com.sessionflow.event.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.sessionflow.dto.ResourceChangedNotification;
import com.sessionflow.event.ResourceChangedEvent;
import com.sessionflow.mapper.ResourceChangedNotificationMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ResourceChangedEventListener {
    
    private final ResourceChangedNotificationMapper resourceChangedNotificationMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @EventListener
    public <T> void onResourceChanged(ResourceChangedEvent<T> event) {
        // 將 event 內容轉成 WebSocket notification DTO
        // 通知對應用戶端
        ResourceChangedNotification<T> notification = resourceChangedNotificationMapper.toNotification(event);
        messagingTemplate.convertAndSend("/topic/notification", notification);
    }
}
