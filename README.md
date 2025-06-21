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

## 目錄

- [安裝與啟動](#安裝與啟動)
- [使用說明](#使用說明)

## 安裝與啟動

### 系統需求
- **Java**: 17 或更高版本

### Quick Start

1.  **下載最新的 Release**
    - 前往 [**GitHub Releases**](https://github.com/l-zch/sessionflow/releases/latest) 頁面。
    - 下載最新版本的 `sessionflow.jar` 檔案。

2.  **執行應用程式**
    - 在終端機中，切換到您下載 JAR 檔案的目錄，然後執行以下指令：
      ```bash
      java -jar sessionflow.jar
      ```

### 可用選項

您可以在執行時附加以下選項：

- `--port <number>`: 指定應用程式運行的埠號 (預設: 8080)。
- `--verbose`: 顯示執行的 SQL 查詢。
- `--debug`: 顯示更詳細的日誌，用於偵錯。

**範例:**
```bash
# 在埠號 8081 上以偵錯模式啟動
java -jar sessionflow.jar --port 8081 --debug
```

## Build from source

### 系統需求
- **Java**: 17 或更高版本
- **Maven**: 3.6+ 

### 從原始碼建置

###  **複製儲存庫**
    ```bash
    git clone https://github.com/l-zch/sessionflow.git
    cd sessionflow
    ```

### 編譯與執行

**編譯**
```bash
mvn clean compile
```
    
**執行開發伺服器**
```bash
mvn spring-boot:run
```

### 測試

**執行測試**
```bash
mvn test
```

**覆蓋率報告**
```bash
mvn clean verify
open target/site/jacoco/index.html
```
    
### **打包**
```bash
mvn clean package -DskipTests
```
產生的檔案會在 `target/sessionflow.jar`

### 環境設定

#### 資料庫配置
系統預設使用 H2 資料庫，資料儲存在專案根目錄的 `sessionflow_db.mv.db` 檔案中。

**H2 資料庫設定 (`application.properties`)** 
```properties
spring.datasource.url=jdbc:h2:file:./sessionflow_db;MODE=MySQL;AUTO_SERVER=TRUE
spring.datasource.username=sa
spring.datasource.password=password
```

#### 開發環境日誌
```properties
# 啟用 H2 控制台
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# 開發日誌
logging.level.com.sessionflow=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

### 依賴與版本

#### 核心依賴
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.3</version>
</parent>
<properties>
    <java.version>17</java.version>
    <lombok.version>1.18.30</lombok.version>
</properties>
```

#### 主要依賴套件
- **Spring Boot 3.5.3**: 核心框架
- **Spring Data JPA**: 資料持久化
- **Spring Boot WebSocket**: WebSocket 支援
- **Spring Boot Validation**: 資料驗證
- **H2 Database**: 預設資料庫
- **Lombok 1.18.30**: 減少樣板程式碼
- **SpringDoc OpenAPI 2.7.0**: API 文檔自動生成

#### 測試與工具
- **JaCoCo 0.8.13**: 程式碼覆蓋率報告
- **Maven Surefire 3.1.2**: 測試執行
- **Spring Boot Test**: 整合測試支援
---

## 使用說明

### 訪問應用

應用程式啟動後，將在以下位址開啟服務：
- **Web App**: http://localhost:8080/sessionflowapp
- **API 文檔**: http://localhost:8080/swagger-ui.html
- **H2 資料庫控制台**: http://localhost:8080/h2-console

若使用 `--port` 選項，將 `8080` 替換為指定的埠號。


### API 操作與 WebSocket 範例

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


#### WebSocket 連接範例
- [WebSocket 文檔](https://github.com/l-zch/sessionflow/blob/main/resource/ws-doc.md)
- [WebSocket 連接範例](https://github.com/l-zch/sessionflow/blob/main/resource/ws-example.html)


### WebApp
[source repo](https://github.com/l-zch/sessionflow-frontend)
###### 部分功能尚未在 WebApp 實現