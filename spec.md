# Task Management System Specification

## 1. Overview

**核心概念**:

-   任務 (Task): 使用者設定的工作目標，可建立多個排程與工作階段。
-   工作階段 (Session): 定義一次可計時的工作設定，包含時間提醒與備註，可關聯任務。
-   階段紀錄 (SessionRecord): 紀錄單次工作階段的實際開始與結束時間，以及執行情況。
-   排程 (ScheduleEntry): 在特定時間的計畫安排，具有起始與結束時間，可選擇關聯任務。

## 2. 功能需求

### 2.1 Task

-   建立／更新／刪除任務
-   查詢所有任務, 可依狀態 (pending/completed) 過濾

### 2.2 Session

-   建立工作階段
-   結束工作階段，產生一筆紀錄並自動刪除該工作階段
-   查詢目前所有工作階段

### 2.3 SessionRecord

-   結束工作階段時，產生一筆紀錄
-   更新／刪除工作階段紀錄
-   查詢指定時間段的所有工作階段紀錄
-   查詢指定任務的所有工作階段紀錄

### 2.4 ScheduleEntry

-   建立／更新／刪除排程
-   查詢指定時間段的所有排程

### 2.5 Tag

-   建立／更新／刪除標籤
-   查詢所有標籤

## 3. 資料模型

### 3.1 Task

```
Task {
  id:Long;
  title: String;
  tags?: Tag[];
  dueTime?: LocalDateTime;
  completedAt?: LocalDateTime;
  note?: String;
  status: 'complete' | 'pending';
  createdAt: LocalDateTime;
  updatedAt: LocalDateTime;
}
```

DTO:

```
TaskRequest {
  title: String;
  tagIds?: Long[];
  dueTime?: LocalDateTime;
  note?: String;
}
```

```
TaskResponse {
  id:Long;
  title: String;
  tags?: Tag[];
  dueTime?: LocalDateTime;
  completedAt?: LocalDateTime;
  note?: String;
  status: 'complete' | 'pending';
}
```

### 3.2 Session

```
Session {
  id: Long;
  title: String;
  task?: Task;
  endReminder?: LocalDateTime;
  note?: String;
}
```

DTO:

```
SessionRequest {
  title: String;
  taskId?: Long;
  endReminder?: LocalDateTime;
  note?: String;
}

SessionResponse {
  id: Long;
  title: String;
  taskId?: Long;
  endReminder?: LocalDateTime;
  note?: String;
}
```

### 3.3 SessionRecord

```
SessionRecord {
  id: Long;
  title: String;
  task?: Task;
  startAt: LocalDateTime;
  endAt: LocalDateTime;
  plannedNote?: String; // from Session
  completionNote?: String;
}
```

DTO:

```
SessionRecordCreateRequest {
  sessionId: Long;
  completionNote?: String;
}

SessionRecordUpdateRequest {
  plannedNote?: String;
  completionNote?: String;
}

SessionRecordResponse {
  id: Long;
  title: String;
  taskId?: Long;
  startAt: LocalDateTime;
  endAt: LocalDateTime;
  plannedNote?: String;
  completionNote?: String;
}
```

### 3.4 ScheduleEntry

```
ScheduleEntry {
  id: Long;
  title: String;
  task?: Task;
  startAt: LocalDateTime;
  endAt: LocalDateTime; // endAt should be after startAt
  note?: String;
}
```

DTO:

```
ScheduleEntryRequest {
  title: String;
  taskId?: Long;
  startAt: LocalDateTime;
  endAt: LocalDateTime;
  note?: String;
}

ScheduleEntryResponse {
  id: Long;
  title: String;
  taskId?: Long;
  startAt: LocalDateTime;
  endAt: LocalDateTime;
  note?: String;
}
```

### 3.5 Tag

```
Tag {
  id: Long;
  name: String;  // name should be unique
  color: string; // hex color code
}
```

DTO:

```
TagRequest {
  name: String;
  color: string;
}

TagResponse {
  id: Long;
  name: String;
  color: string;
}
```

## 4. API 規範

### 4.1 Task Endpoints

#### 4.1.1 建立任務
- **方法**: `POST`
- **路徑**: `/api/tasks`
- **描述**: 建立新的任務
- **請求體**: `TaskRequest`
- **HTTP 狀態碼**: `201 Created`
- **回應體**: `TaskResponse`

**請求範例**:
```json
{
  "title": "完成專案文件",
  "tagIds": [1, 2],
  "dueTime": "2024-01-15T18:00:00",
  "note": "需要包含技術規格和使用者手冊"
}
```

**回應範例**:
```json
{
  "id": 1,
  "title": "完成專案文件",
  "tags": [
    {"id": 1, "name": "工作", "color": "#FF5733"},
    {"id": 2, "name": "重要", "color": "#33FF57"}
  ],
  "dueTime": "2024-01-15T18:00:00",
  "completedAt": null,
  "note": "需要包含技術規格和使用者手冊",
  "status": "pending"
}
```

#### 4.1.2 查詢所有任務
- **方法**: `GET`
- **路徑**: `/api/tasks`
- **描述**: 查詢所有任務，可依狀態過濾
- **查詢參數**: 
  - `status` (可選): `pending` | `complete`
- **HTTP 狀態碼**: `200 OK`
- **回應體**: `List<TaskResponse>`

**請求範例**: `GET /api/tasks?status=pending`

**回應範例**:
```json
[
  {
    "id": 1,
    "title": "完成專案文件",
    "tags": [
      {"id": 1, "name": "工作", "color": "#FF5733"}
    ],
    "dueTime": "2024-01-15T18:00:00",
    "completedAt": null,
    "note": "需要包含技術規格和使用者手冊",
    "status": "pending"
  }
]
```

#### 4.1.3 更新任務
- **方法**: `PUT`
- **路徑**: `/api/tasks/{id}`
- **描述**: 更新指定任務
- **路徑參數**: `id` (Long) - 任務 ID
- **請求體**: `TaskRequest`
- **HTTP 狀態碼**: `200 OK`
- **回應體**: `TaskResponse`

**請求範例**:
```json
{
  "title": "完成專案文件（已更新）",
  "tagIds": [1, 3],
  "dueTime": "2024-01-20T18:00:00",
  "note": "需要包含技術規格、使用者手冊和部署指南"
}
```

#### 4.1.4 完成任務
- **方法**: `PATCH`
- **路徑**: `/api/tasks/{id}/complete`
- **描述**: 標記任務為完成
- **路徑參數**: `id` (Long) - 任務 ID
- **HTTP 狀態碼**: `200 OK`
- **回應體**: `TaskResponse`

#### 4.1.5 刪除任務
- **方法**: `DELETE`
- **路徑**: `/api/tasks/{id}`
- **描述**: 刪除指定任務
- **路徑參數**: `id` (Long) - 任務 ID
- **HTTP 狀態碼**: `204 No Content`

### 4.2 Session Endpoints

#### 4.2.1 建立工作階段
- **方法**: `POST`
- **路徑**: `/api/sessions`
- **描述**: 建立新的工作階段
- **請求體**: `SessionRequest`
- **HTTP 狀態碼**: `201 Created`
- **回應體**: `SessionResponse`

**請求範例**:
```json
{
  "title": "專案開發時間",
  "taskId": 1,
  "endReminder": "2024-01-15T16:00:00",
  "note": "專注於核心功能開發"
}
```

**回應範例**:
```json
{
  "id": 1,
  "title": "專案開發時間",
  "taskId": 1,
  "endReminder": "2024-01-15T16:00:00",
  "note": "專注於核心功能開發"
}
```

#### 4.2.2 查詢目前所有工作階段
- **方法**: `GET`
- **路徑**: `/api/sessions`
- **描述**: 查詢目前所有工作階段
- **HTTP 狀態碼**: `200 OK`
- **回應體**: `List<SessionResponse>`

**回應範例**:
```json
[
  {
    "id": 1,
    "title": "專案開發時間",
    "taskId": 1,
    "endReminder": "2024-01-15T16:00:00",
    "note": "專注於核心功能開發"
  },
  {
    "id": 2,
    "title": "文件撰寫",
    "taskId": null,
    "endReminder": "2024-01-15T18:00:00",
    "note": "完成使用者手冊"
  }
]
```

#### 4.2.3 結束工作階段
- **方法**: `POST`
- **路徑**: `/api/sessions/{id}/end`
- **描述**: 結束工作階段並產生紀錄，同時刪除該工作階段
- **路徑參數**: `id` (Long) - 工作階段 ID
- **請求體**: `SessionRecordCreateRequest`
- **HTTP 狀態碼**: `201 Created`
- **回應體**: `SessionRecordResponse`

**請求範例**:
```json
{
  "sessionId": 1,
  "completionNote": "完成了主要功能的 80%"
}
```

### 4.3 SessionRecord Endpoints

#### 4.3.1 查詢工作階段紀錄
- **方法**: `GET`
- **路徑**: `/api/session-records`
- **描述**: 查詢指定時間段的所有工作階段紀錄
- **查詢參數**:
  - `startDate` (可選): `LocalDate` - 開始日期
  - `endDate` (可選): `LocalDate` - 結束日期
  - `taskId` (可選): `Long` - 任務 ID
- **HTTP 狀態碼**: `200 OK`
- **回應體**: `List<SessionRecordResponse>`

**請求範例**: `GET /api/session-records?startDate=2024-01-01&endDate=2024-01-31&taskId=1`

**回應範例**:
```json
[
  {
    "id": 1,
    "title": "專案開發時間",
    "taskId": 1,
    "startAt": "2024-01-15T14:00:00",
    "endAt": "2024-01-15T16:00:00",
    "plannedNote": "專注於核心功能開發",
    "completionNote": "完成了主要功能的 80%"
  }
]
```

#### 4.3.2 更新工作階段紀錄
- **方法**: `PUT`
- **路徑**: `/api/session-records/{id}`
- **描述**: 更新工作階段紀錄
- **路徑參數**: `id` (Long) - 紀錄 ID
- **請求體**: `SessionRecordUpdateRequest`
- **HTTP 狀態碼**: `200 OK`
- **回應體**: `SessionRecordResponse`

**請求範例**:
```json
{
  "plannedNote": "專注於核心功能開發和測試",
  "completionNote": "完成了主要功能的 90%，還需要進行單元測試"
}
```

#### 4.3.3 刪除工作階段紀錄
- **方法**: `DELETE`
- **路徑**: `/api/session-records/{id}`
- **描述**: 刪除工作階段紀錄
- **路徑參數**: `id` (Long) - 紀錄 ID
- **HTTP 狀態碼**: `204 No Content`

### 4.4 ScheduleEntry Endpoints

#### 4.4.1 建立排程
- **方法**: `POST`
- **路徑**: `/api/schedule-entries`
- **描述**: 建立新的排程
- **請求體**: `ScheduleEntryRequest`
- **HTTP 狀態碼**: `201 Created`
- **回應體**: `ScheduleEntryResponse`

**請求範例**:
```json
{
  "title": "團隊會議",
  "taskId": 1,
  "startAt": "2024-01-15T10:00:00",
  "endAt": "2024-01-15T11:00:00",
  "note": "討論專案進度和下週計畫"
}
```

**回應範例**:
```json
{
  "id": 1,
  "title": "團隊會議",
  "taskId": 1,
  "startAt": "2024-01-15T10:00:00",
  "endAt": "2024-01-15T11:00:00",
  "note": "討論專案進度和下週計畫"
}
```

#### 4.4.2 查詢排程
- **方法**: `GET`
- **路徑**: `/api/schedule-entries`
- **描述**: 查詢指定時間段的所有排程
- **查詢參數**:
  - `startDate` (必填): `LocalDate` - 開始日期
  - `endDate` (必填): `LocalDate` - 結束日期
- **HTTP 狀態碼**: `200 OK`
- **回應體**: `List<ScheduleEntryResponse>`

**請求範例**: `GET /api/schedule-entries?startDate=2024-01-15&endDate=2024-01-15`

**回應範例**:
```json
[
  {
    "id": 1,
    "title": "團隊會議",
    "taskId": 1,
    "startAt": "2024-01-15T10:00:00",
    "endAt": "2024-01-15T11:00:00",
    "note": "討論專案進度和下週計畫"
  },
  {
    "id": 2,
    "title": "客戶電話",
    "taskId": null,
    "startAt": "2024-01-15T14:00:00",
    "endAt": "2024-01-15T15:00:00",
    "note": "確認需求變更"
  }
]
```

#### 4.4.3 更新排程
- **方法**: `PUT`
- **路徑**: `/api/schedule-entries/{id}`
- **描述**: 更新指定排程
- **路徑參數**: `id` (Long) - 排程 ID
- **請求體**: `ScheduleEntryRequest`
- **HTTP 狀態碼**: `200 OK`
- **回應體**: `ScheduleEntryResponse`

**請求範例**:
```json
{
  "title": "團隊會議（延長）",
  "taskId": 1,
  "startAt": "2024-01-15T10:00:00",
  "endAt": "2024-01-15T12:00:00",
  "note": "討論專案進度、下週計畫和技術問題"
}
```

#### 4.4.4 刪除排程
- **方法**: `DELETE`
- **路徑**: `/api/schedule-entries/{id}`
- **描述**: 刪除指定排程
- **路徑參數**: `id` (Long) - 排程 ID
- **HTTP 狀態碼**: `204 No Content`

### 4.5 Tag Endpoints

#### 4.5.1 建立標籤
- **方法**: `POST`
- **路徑**: `/api/tags`
- **描述**: 建立新的標籤
- **請求體**: `TagRequest`
- **HTTP 狀態碼**: `201 Created`
- **回應體**: `TagResponse`

**請求範例**:
```json
{
  "name": "工作",
  "color": "#FF5733"
}
```

**回應範例**:
```json
{
  "id": 1,
  "name": "工作",
  "color": "#FF5733"
}
```

#### 4.5.2 查詢所有標籤
- **方法**: `GET`
- **路徑**: `/api/tags`
- **描述**: 查詢所有標籤
- **HTTP 狀態碼**: `200 OK`
- **回應體**: `List<TagResponse>`

**回應範例**:
```json
[
  {
    "id": 1,
    "name": "工作",
    "color": "#FF5733"
  },
  {
    "id": 2,
    "name": "重要",
    "color": "#33FF57"
  }
]
```

#### 4.5.3 更新標籤
- **方法**: `PUT`
- **路徑**: `/api/tags/{id}`
- **描述**: 更新指定標籤
- **路徑參數**: `id` (Long) - 標籤 ID
- **請求體**: `TagRequest`
- **HTTP 狀態碼**: `200 OK`
- **回應體**: `TagResponse`

**請求範例**:
```json
{
  "name": "工作（重要）",
  "color": "#FF6B35"
}
```

#### 4.5.4 刪除標籤
- **方法**: `DELETE`
- **路徑**: `/api/tags/{id}`
- **描述**: 刪除指定標籤
- **路徑參數**: `id` (Long) - 標籤 ID
- **HTTP 狀態碼**: `204 No Content`

### 4.6 統一錯誤處理

#### 錯誤回應格式
所有 API 錯誤都使用統一格式：

```json
{
  "code": "ERROR_CODE",
  "message": "錯誤描述",
  "details": "詳細錯誤資訊",
  "timestamp": "2024-01-15T10:00:00"
}
```

#### 常見錯誤狀態碼
- `400 Bad Request`: 請求參數錯誤或驗證失敗
- `404 Not Found`: 資源不存在
- `409 Conflict`: 資源衝突（如標籤名稱重複）
- `422 Unprocessable Entity`: 業務邏輯錯誤（如結束時間早於開始時間）
- `500 Internal Server Error`: 伺服器內部錯誤

**錯誤範例**:
```json
{
  "code": "VALIDATION_ERROR",
  "message": "請求參數驗證失敗",
  "details": "title 欄位不能為空",
  "timestamp": "2024-01-15T10:00:00"
}
```

### 4.7 通用標頭
所有 API 請求應包含：
- `Content-Type: application/json`
- `Accept: application/json`

## 5. 非功能需求

-   **錯誤處理**: 統一錯誤碼與回傳格式
-   **日誌與監控**: 紀錄 API 呼叫與錯誤
-   **擴充性**: 資料庫設計支援橫向擴充

## 6. 軟體架構與技術選型

### 軟體架構

-   採用 Layered MVC 架構：

    -   Presentation 層（Controller）：處理 HTTP 請求與回應
    -   Service 層：實現業務邏輯
    -   Repository 層：進行資料存取
    -   Model 層：定義系統實體

### 技術選型

-   **後端框架**：Spring Boot
-   **ORM**：Hibernate
-   **建構工具**：Maven
-   **資料庫**：H2 嵌入式（檔案式儲存）或 SQLite
-   **打包與部署**：Spring Boot fat-jar，支援 Mac 本地端離線執行
-   **測試策略**：JUnit 5 + Mockito