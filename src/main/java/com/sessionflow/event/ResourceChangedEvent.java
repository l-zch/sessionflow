package com.sessionflow.event;

import java.time.Instant;
import java.util.List;

import com.sessionflow.common.NotificationType;
import com.sessionflow.dto.ResourceChangedNotification.Affected;

/**
 * 資源變更事件
 * 
 * @param notificationType 通知類型
 * @param id               資源 ID
 * @param ids              資源 ID 列表 (batch 操作時使用)
 * @param data             資源內容 (僅 created/updated 帶)
 * @param affected         副作用 (選填)
 * @param occurredAt       事件發生時間
 */
public record ResourceChangedEvent<T>(
        NotificationType notificationType,
        Long id,
        List<Long> ids,
        T data,
        List<Affected> affected,
        Instant occurredAt) {

    // 主建構式，設定 occurredAt 預設為現在
    public ResourceChangedEvent(
            NotificationType notificationType,
            Long id,
            List<Long> ids,
            T data,
            List<Affected> affected) {
        this(notificationType, id, ids, data, affected, Instant.now());
    }

}
