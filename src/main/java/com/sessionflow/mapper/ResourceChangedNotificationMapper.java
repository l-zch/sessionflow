package com.sessionflow.mapper;

import com.sessionflow.dto.ResourceChangedNotification;
import com.sessionflow.event.ResourceChangedEvent;

public interface ResourceChangedNotificationMapper {

    <T> ResourceChangedNotification<T> toNotification(ResourceChangedEvent<T> event);
}
