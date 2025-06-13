package com.sessionflow.mapper.impl;

import com.sessionflow.dto.ResourceChangedNotification;
import com.sessionflow.event.ResourceChangedEvent;
import com.sessionflow.mapper.ResourceChangedNotificationMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ResourceChangedNotificationMapperImpl implements ResourceChangedNotificationMapper {


    @Override
    public <T> ResourceChangedNotification<T> toNotification(ResourceChangedEvent<T> event) {
        return new ResourceChangedNotification<>(event.notificationType(), event.id(), event.ids(), event.data(), event.affected(), event.occurredAt().toEpochMilli());
    }
    
}
