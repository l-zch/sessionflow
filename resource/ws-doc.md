# WebSocket 事件推播文檔

## 連接設定

### 連接端點
- **URL**: `http://localhost:8080/ws`
- **協議**: STOMP over WebSocket
- **SockJS 支援**: 是
- **訂閱頻道**: `/topic/notification`

### 連接範例 (JavaScript)
```javascript
// 使用 SockJS + STOMP
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    // 訂閱通知頻道
    stompClient.subscribe('/topic/notification', function(message) {
        const notification = JSON.parse(message.body);
        handleNotification(notification);
    });
});
```

## 推播事件類型

系統會在以下資源異動時觸發 WebSocket 推播：

### 任務 (Task) 相關事件
- `task_create` - 建立任務
- `task_update` - 更新任務（包含完成、重新開啟）
- `task_delete` - 刪除任務

### 工作階段 (Session) 相關事件
- `session_create` - 建立工作階段
- `session_delete` - 刪除工作階段

### 工作紀錄 (SessionRecord) 相關事件
- `session_record_create` - 建立工作紀錄
- `session_record_update` - 更新工作紀錄
- `session_record_delete` - 刪除工作紀錄

### 排程項目 (ScheduleEntry) 相關事件
- `schedule_entry_create` - 建立排程項目
- `schedule_entry_update` - 更新排程項目
- `schedule_entry_delete` - 刪除排程項目

### 標籤 (Tag) 相關事件
- `tag_create` - 建立標籤
- `tag_update` - 更新標籤
- `tag_delete` - 刪除標籤

## 推播內容結構

### 基本結構
```typescript
interface ResourceChangedNotification<T> {
    notificationType: string;    // 事件類型
    id?: number;                 // 單筆資源 ID
    ids?: number[];              // 多筆資源 ID 列表
    data?: T;                    // 資源內容（僅 create/update 事件）
    affected?: Affected[];       // 級聯影響的資源
    timestamp: number;           // 事件發生時間戳記
}

interface Affected {
    notificationType: string;    // 受影響資源的事件類型
    ids: number[];               // 受影響資源的 ID 列表
}
```

### 資源內容結構

#### TaskResponse
```typescript
interface TaskResponse {
    id: number;
    title: string;
    tags: TagResponse[];
    dueTime?: string;           // ISO 8601 格式
    completedAt?: string;       // ISO 8601 格式
    note?: string;
    status: "PENDING" | "COMPLETE";
}
```

#### SessionResponse
```typescript
interface SessionResponse {
    id: number;
    title: string;
    taskId?: number;
    endReminder?: string;       // ISO 8601 格式
    note?: string;
}
```

#### TagResponse
```typescript
interface TagResponse {
    id: number;
    name: string;
    color: string;
}
```

## 推播範例

### 1. 建立任務
```json
{
    "notificationType": "task_create",
    "id": 123,
    "ids": null,
    "data": {
        "id": 123,
        "title": "完成專案文件",
        "tags": [
            {
                "id": 1,
                "name": "工作",
                "color": "#FF5722"
            }
        ],
        "dueTime": "2024-01-15T18:00:00",
        "completedAt": null,
        "note": "需要包含技術規格和使用者手冊",
        "status": "PENDING"
    },
    "affected": null,
    "timestamp": 1704441600000
}
```

### 2. 更新任務
```json
{
    "notificationType": "task_update",
    "id": 123,
    "ids": null,
    "data": {
        "id": 123,
        "title": "完成專案文件",
        "tags": [
            {
                "id": 1,
                "name": "工作",
                "color": "#FF5722"
            }
        ],
        "dueTime": "2024-01-15T18:00:00",
        "completedAt": "2024-01-14T16:30:00",
        "note": "需要包含技術規格和使用者手冊",
        "status": "COMPLETE"
    },
    "affected": null,
    "timestamp": 1704441600000
}
```

### 3. 刪除任務（含級聯影響）
```json
{
    "notificationType": "task_delete",
    "id": 123,
    "ids": null,
    "data": null,
    "affected": [
        {
            "notificationType": "session_delete",
            "ids": [456, 789]
        },
        {
            "notificationType": "session_record_delete",
            "ids": [101, 102, 103]
        },
        {
            "notificationType": "schedule_entry_delete",
            "ids": [201]
        }
    ],
    "timestamp": 1704441600000
}
```

### 4. 建立工作階段
```json
{
    "notificationType": "session_create",
    "id": 456,
    "ids": null,
    "data": {
        "id": 456,
        "title": "專案開發時間",
        "taskId": 123,
        "endReminder": "2024-01-15T16:00:00",
        "note": "專注於核心功能開發"
    },
    "affected": null,
    "timestamp": 1704441600000
}
```

## 前端處理建議

### 事件處理器範例
```javascript
function handleNotification(notification) {
    const { notificationType, id, ids, data, affected } = notification;
    
    switch (notificationType) {
        case 'task_create':
            // 新增任務到列表
            addTaskToList(data);
            break;
            
        case 'task_update':
            // 更新任務資料
            updateTaskInList(id, data);
            break;
            
        case 'task_delete':
            // 移除任務並處理級聯影響
            removeTaskFromList(id);
            if (affected) {
                handleAffectedResources(affected);
            }
            break;
            
        case 'session_create':
            addSessionToList(data);
            break;
            
        // ... 其他事件類型
    }
}

function handleAffectedResources(affected) {
    affected.forEach(item => {
        const { notificationType, ids } = item;
        
        switch (notificationType) {
            case 'session_delete':
                ids.forEach(id => removeSessionFromList(id));
                break;
            case 'session_record_delete':
                ids.forEach(id => removeSessionRecordFromList(id));
                break;
            // ... 其他受影響的資源類型
        }
    });
}
```

### 連接狀態管理
```javascript
let reconnectAttempts = 0;
const maxReconnectAttempts = 5;

function connect() {
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);
    
    stompClient.connect({}, 
        function(frame) {
            console.log('WebSocket connected');
            reconnectAttempts = 0;
            
            stompClient.subscribe('/topic/notification', function(message) {
                const notification = JSON.parse(message.body);
                handleNotification(notification);
            });
        },
        function(error) {
            console.error('WebSocket connection failed:', error);
            
            if (reconnectAttempts < maxReconnectAttempts) {
                reconnectAttempts++;
                setTimeout(() => connect(), 5000 * reconnectAttempts);
            }
        }
    );
}
```

## 注意事項

1. **時間格式**: 所有時間欄位使用 ISO 8601 格式 (`YYYY-MM-DDTHH:mm:ss`)
2. **時間戳記**: `timestamp` 欄位為 Unix 時間戳記（毫秒）
3. **級聯刪除**: 刪除操作可能影響其他資源，需處理 `affected` 欄位
4. **空值處理**: `data` 在刪除事件中為 `null`，`affected` 在一般事件中為 `null`
5. **重連機制**: 建議實作自動重連機制以確保連接穩定性
6. **錯誤處理**: 需處理 JSON 解析錯誤和連接中斷情況 