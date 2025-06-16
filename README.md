# SessionFlow - 任務管理與工作階段追蹤系統

## 專案概述

SessionFlow 是一個任務管理系統，支援任務建立、工作階段追蹤、時間記錄和排程管理。

### 核心功能
- **任務管理 (Task)**: 建立、更新、標記完成任務，支援標籤分類和截止日期
- **工作階段 (Session)**: 可計時的工作設定，包含時間提醒與備註
- **階段紀錄 (SessionRecord)**: 記錄實際工作時間和執行情況
- **排程管理 (ScheduleEntry)**: 時間計畫安排，可關聯特定任務
- **標籤系統 (Tag)**: 任務分類和顏色標記
- **即時通知 (WebSocket)**: 跨裝置資料同步和變更通知

## 安裝與建置

### 系統需求
- **Java**: 17 或更高版本
- **Maven**: 3.6+ 

### 快速安裝
```bash
git clone https://github.com/l-zch/sessionflow.git
cd sessionflow
mvn clean compile
mvn spring-boot:run
```

#### 執行測試
```bash
mvn test
```
#### 覆蓋率報告
```bash
mvn clean verify

open target/site/jacoco/index.html
```

### 生產環境部署
```bash
mvn clean package -DskipTests

java -jar target/sessionflow-0.0.1-SNAPSHOT.jar
```

## 環境設定

### 資料庫配置
系統預設使用 H2 資料庫，資料儲存在專案根目錄的 `sessionflow_db.mv.db` 檔案中。

**H2 資料庫設定** 
```properties
spring.datasource.url=jdbc:h2:file:./sessionflow_db;MODE=MySQL;AUTO_SERVER=TRUE
spring.datasource.username=sa
spring.datasource.password=password
```

### 開發環境配置
```properties
# 啟用 H2 控制台
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# 開發日誌
logging.level.com.sessionflow=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

## 快速上手範例

### 1. 啟動應用程式
```bash
mvn spring-boot:run
```

### 2. 訪問 Web 介面
- **API 文檔**: http://localhost:8080/swagger-ui.html
- **H2 控制台**: http://localhost:8080/h2-console
- **WebSocket 端點**: http://localhost:8080/ws

### 3. 基本 API 操作

#### 建立任務
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "完成專案文件",
    "note": "包含技術規格和使用者手冊",
    "dueTime": "2024-01-15T18:00:00"
  }'
```

#### 查詢所有任務
```bash
curl http://localhost:8080/api/tasks
```

#### 建立工作階段
```bash
curl -X POST http://localhost:8080/api/sessions \
  -H "Content-Type: application/json" \
  -d '{
    "title": "專案開發時間",
    "taskId": 1,
    "endReminder": "2024-01-15T16:00:00"
  }'
```

### 4. WebSocket 連接範例
- [WebSocket 文檔](https://github.com/l-zch/sessionflow/resource/ws_doc.md)
- [WebSocket 連接範例](https://github.com/l-zch/sessionflow/resrouce/ws-example.html)

#### JavaScript 客戶端
```javascript
// 使用 SockJS + STOMP
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    // 訂閱資料更新通知
    stompClient.subscribe('/topic/notification', function(notification) {
        const data = JSON.parse(notification.body);
        console.log('收到通知:', data);
        
        // 處理不同類型的通知
        switch(data.notificationType) {
            case 'task_create':
                handleTaskCreate(data);
                break;
            case 'task_delete':
                handleTaskDelete(data);
                break;
            // 其他通知類型...
        }
    });
});
```

## 功能模組說明

### 任務管理 (Task)
- **建立任務**: 設定標題、備註、截止日期和標籤
- **狀態管理**: PENDING（待處理）、COMPLETE（已完成）
- **標籤關聯**: 支援多標籤分類和顏色標記
- **級聯刪除**: 刪除任務時自動清理相關的工作階段、記錄和排程

### 工作階段 (Session)
- **計時設定**: 建立可計時的工作設定
- **提醒功能**: 設定結束時間提醒
- **任務關聯**: 可選擇關聯特定任務
- **自動記錄**: 結束時自動產生 SessionRecord

### 階段紀錄 (SessionRecord)
- **時間追蹤**: 記錄實際開始和結束時間
- **備註管理**: 計畫備註和完成備註分別記錄
- **查詢功能**: 支援時間範圍和任務篩選
- **統計分析**: 提供工作時間統計基礎

### 排程管理 (ScheduleEntry)
- **時間計畫**: 設定具體的開始和結束時間
- **任務關聯**: 可選擇關聯特定任務
- **衝突檢查**: 時間範圍驗證（結束時間須晚於開始時間）
- **查詢篩選**: 支援時間範圍查詢

### 標籤系統 (Tag)
- **分類管理**: 為任務提供分類功能
- **顏色標記**: 支援十六進位顏色代碼
- **唯一性約束**: 標籤名稱必須唯一
- **關聯管理**: 自動處理任務與標籤的多對多關係

### WebSocket 即時通知
- **事件類型**: 支援 task_create、task_update、task_delete 等 15 種事件
- **級聯通知**: 刪除任務時推播受影響的相關資源
- **資料同步**: 跨裝置即時同步資料變更
- **連接管理**: 支援 SockJS 回退機制，確保連接穩定性

## 依賴與版本要求

### 核心依賴
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.4.6</version>
</parent>

<properties>
    <java.version>17</java.version>
    <lombok.version>1.18.30</lombok.version>
</properties>
```

### 主要依賴套件
- **Spring Boot 3.4.6**: 核心框架
- **Spring Data JPA**: 資料持久化
- **Spring Boot WebSocket**: WebSocket 支援
- **Spring Boot Validation**: 資料驗證
- **H2 Database**: 預設資料庫
- **Lombok 1.18.30**: 減少樣板程式碼
- **SpringDoc OpenAPI 2.7.0**: API 文檔自動生成

### 測試與工具
- **JaCoCo 0.8.10**: 程式碼覆蓋率報告
- **Maven Surefire 3.1.2**: 測試執行
- **Spring Boot Test**: 整合測試支援