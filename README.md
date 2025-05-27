# SessionFlow

SessionFlow 是一個任務管理系統，具有工作階段追蹤功能，專為提高個人或團隊工作效率而設計。

## 專案概述

SessionFlow 讓使用者能夠：

-   管理任務
-   為任務排程工作階段
-   追蹤工作時間與進度
-   查看工作統計資料

### 任務管理
- 建立、更新、刪除任務
- 任務狀態追蹤 (PENDING/COMPLETE)
- 任務標籤系統

### 工作階段
- 開始/結束工作階段
- 工作階段記錄保存
- 時間追蹤與統計

### 排程管理
- 任務排程功能
- 時間範圍管理

## 技術棧

### 後端框架
-   **Spring Boot**: 3.4.6
-   **Spring Data JPA**: 資料持久層
-   **Spring Boot Validation**: 資料驗證
-   **Spring Boot Actuator**: 應用程式監控

### 資料庫
-   **H2 Database**: 檔案式資料庫 (主要)
-   **SQLite**: 備選資料庫支援
-   **Hibernate**: ORM 框架

### 開發工具
-   **Java**: 17
-   **Maven**: 構建工具
-   **Lombok**: 減少樣板程式碼

### API 文件
-   **SpringDoc OpenAPI**: 2.7.0 (Swagger UI)

### 測試與品質
-   **Spring Boot Test**: 單元測試與整合測試
-   **JaCoCo**: 程式碼覆蓋率分析
-   **Maven Surefire**: 測試執行

### 配置
-   **YAML**: 應用程式配置格式
-   **時區**: Asia/Taipei

## 快速開始

### 環境需求

-   JDK 17 或以上
-   Maven 3.6 或以上

### 建置與執行

1. 複製專案

```
git clone [repository-url]
cd sessionflow-backend
```

2. 使用 Maven 建置專案

```
mvn clean package
```

3. 執行應用程式

```
java -jar target/sessionflow-0.0.1-SNAPSHOT.jar
```

4. 應用程式將在 http://localhost:8080 啟動，H2 資料庫控制台可在 http://localhost:8080/h2-console 訪問

-   JDBC URL: jdbc:h2:file:./sessionflow_db
-   用戶名: sa
-   密碼: password

## API 文件

-   **Swagger UI**: http://localhost:8080/swagger-ui.html
-   **OpenAPI JSON**: http://localhost:8080/api-docs

## 專案結構

```
src/
├── main/
│   ├── java/com/sessionflow/
│   │   ├── config/          # 配置類別
│   │   ├── controller/      # REST API 控制器
│   │   ├── dto/            # 資料傳輸物件
│   │   ├── exception/      # 自定義例外處理
│   │   ├── mapper/         # DTO-Entity 轉換器
│   │   ├── model/          # 實體類別
│   │   ├── repository/     # 資料存取層
│   │   └── service/        # 業務邏輯層
│   └── resources/
│       └── application.yml # 應用程式配置
└── test/                   # 測試程式碼
```

## 開發指南

### 程式碼品質
```bash
# 執行測試
mvn test

# 生成測試覆蓋率報告
mvn clean verify
# 報告位置: target/site/jacoco/index.html
```

### 資料庫管理
- **開發環境**: H2 檔案資料庫 (`./sessionflow_db.mv.db`)
- **測試環境**: H2 記憶體資料庫
- **控制台**: http://localhost:8080/h2-console

### 日誌配置
- 應用程式日誌: `app.log`
- 測試日誌: `test.log`
- SQL 查詢日誌已啟用 (DEBUG 模式)
