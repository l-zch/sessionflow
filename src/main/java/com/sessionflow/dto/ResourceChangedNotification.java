package com.sessionflow.dto;

import com.sessionflow.common.NotificationType;

import java.util.List;

/**
 * WebSocket 通知
 * 
 * @param notificationType 通知類型
 * @param id               資源 ID
 * @param ids              資源 ID 列表 (batch 操作時使用)
 * @param data             資源內容 (僅 created/updated 帶)
 * @param affected         副作用 (選填)
 * @param timestamp        時間戳記
 */
public record ResourceChangedNotification<T>(
        NotificationType notificationType,
        Long id, // 單筆
        List<Long> ids, // 多筆時使用
        T data, // 對應資源 ResponseDTO，僅 created/updated 帶
        List<Affected> affected, // 副作用（選填）
        long timestamp) {

    /**
     * 副作用
     * 
     * @param notificationType 通知類型
     * @param ids 資源 ID 列表
     */
    public record Affected(
            NotificationType notificationType,
            List<Long> ids) {
    }
}
