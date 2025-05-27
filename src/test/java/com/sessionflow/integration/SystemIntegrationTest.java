package com.sessionflow.integration;

import com.sessionflow.dto.*;
import com.sessionflow.model.*;
import com.sessionflow.repository.*;
import com.sessionflow.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * 系統級整合測試
 * 模擬真實使用流程，驗證整個系統的資料與邏輯行為
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("系統級整合測試")
class SystemIntegrationTest {

    @Autowired
    private TaskService taskService;
    
    @Autowired
    private TagService tagService;
    
    @Autowired
    private SessionService sessionService;
    
    @Autowired
    private SessionRecordService sessionRecordService;
    
    @Autowired
    private ScheduleEntryService scheduleEntryService;
    
    // Repository for verification
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private TagRepository tagRepository;
    
    @Autowired
    private SessionRepository sessionRepository;
    
    @Autowired
    private SessionRecordRepository sessionRecordRepository;
    
    @Autowired
    private ScheduleEntryRepository scheduleEntryRepository;

    @Test
    @DisplayName("完整工作流程：創建標籤 -> 創建任務 -> 創建會話 -> 結束會話並記錄 -> 安排時程")
    void testCompleteWorkflow() {
        // 1. 創建標籤
        TagRequest tagRequest = new TagRequest("工作", "#FF5733");
        TagResponse createdTag = tagService.createTag(tagRequest);
        assertThat(createdTag).isNotNull();
        assertThat(createdTag.getName()).isEqualTo("工作");
        assertThat(createdTag.getColor()).isEqualTo("#FF5733");
        
        // 2. 創建任務（關聯標籤）
        TaskRequest taskRequest = new TaskRequest("完成專案報告");
        taskRequest.setNote("需要包含技術架構和測試結果");
        taskRequest.setDueTime(LocalDateTime.now().plusDays(7));
        taskRequest.setTagIds(List.of(createdTag.getId()));
        
        TaskResponse createdTask = taskService.createTask(taskRequest);
        assertThat(createdTask).isNotNull();
        assertThat(createdTask.getTitle()).isEqualTo("完成專案報告");
        assertThat(createdTask.getStatus()).isEqualTo("PENDING");
        assertThat(createdTask.getTags()).hasSize(1);
        assertThat(createdTask.getTags().get(0).getName()).isEqualTo("工作");
        
        // 3. 創建會話
        SessionRequest sessionRequest = new SessionRequest("專案報告撰寫會話");
        sessionRequest.setTaskId(createdTask.getId());
        sessionRequest.setEndReminder(LocalDateTime.now().plusHours(2));
        sessionRequest.setNote("專注撰寫技術架構部分");
        
        SessionResponse createdSession = sessionService.createSession(sessionRequest);
        assertThat(createdSession).isNotNull();
        assertThat(createdSession.getTitle()).isEqualTo("專案報告撰寫會話");
        assertThat(createdSession.getTaskId()).isEqualTo(createdTask.getId());
        
        // 4. 結束會話並創建記錄
        SessionRecordCreateRequest recordRequest = new SessionRecordCreateRequest();
        recordRequest.setSessionId(createdSession.getId());
        recordRequest.setCompletionNote("完成了基本架構設計");
        recordRequest.setStartTime(LocalDateTime.now().minusHours(1));
        recordRequest.setPlannedNotes("計劃撰寫架構圖");
        
        SessionRecordResponse createdRecord = sessionService.endSession(createdSession.getId(), recordRequest);
        assertThat(createdRecord).isNotNull();
        assertThat(createdRecord.getTitle()).isEqualTo("專案報告撰寫會話");
        assertThat(createdRecord.getTaskId()).isEqualTo(createdTask.getId());
        
        // 5. 安排未來時程
        ScheduleEntryRequest scheduleRequest = new ScheduleEntryRequest();
        scheduleRequest.setTitle("專案報告最終檢查");
        scheduleRequest.setTaskId(createdTask.getId());
        scheduleRequest.setStartAt(LocalDateTime.now().plusDays(6));
        scheduleRequest.setEndAt(LocalDateTime.now().plusDays(6).plusHours(1));
        scheduleRequest.setNote("最終檢查和修正");
        
        ScheduleEntryResponse createdSchedule = scheduleEntryService.createScheduleEntry(scheduleRequest);
        assertThat(createdSchedule).isNotNull();
        assertThat(createdSchedule.getTitle()).isEqualTo("專案報告最終檢查");
        assertThat(createdSchedule.getTaskId()).isEqualTo(createdTask.getId());
        
        // 6. 驗證資料庫狀態
        assertThat(tagRepository.count()).isEqualTo(1);
        assertThat(taskRepository.count()).isEqualTo(1);
        assertThat(sessionRepository.count()).isEqualTo(0); // Session 已被刪除
        assertThat(sessionRecordRepository.count()).isEqualTo(1);
        assertThat(scheduleEntryRepository.count()).isEqualTo(1);
        
        // 7. 完成任務
        TaskResponse completedTask = taskService.completeTask(createdTask.getId());
        assertThat(completedTask.getStatus()).isEqualTo("COMPLETE");
        assertThat(completedTask.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("標籤管理流程：創建 -> 查詢 -> 更新 -> 刪除")
    void testTagManagementWorkflow() {
        // 創建多個標籤
        TagResponse tag1 = tagService.createTag(new TagRequest("緊急", "#FF0000"));
        TagResponse tag2 = tagService.createTag(new TagRequest("重要", "#00FF00"));
        
        // 查詢所有標籤
        List<TagResponse> allTags = tagService.getAllTags();
        assertThat(allTags).hasSize(2);
        assertThat(allTags).extracting(TagResponse::getName)
                .containsExactlyInAnyOrder("緊急", "重要");
        
        // 更新標籤
        TagResponse updatedTag = tagService.updateTag(tag1.getId(), 
                new TagRequest("超緊急", "#FF00FF"));
        
        assertThat(updatedTag.getName()).isEqualTo("超緊急");
        assertThat(updatedTag.getColor()).isEqualTo("#FF00FF");
        
        // 刪除標籤
        tagService.deleteTag(tag2.getId());
        
        List<TagResponse> remainingTags = tagService.getAllTags();
        assertThat(remainingTags).hasSize(1);
        assertThat(remainingTags.get(0).getName()).isEqualTo("超緊急");
    }

    @Test
    @DisplayName("任務狀態管理流程：創建 -> 查詢 -> 完成 -> 狀態篩選")
    void testTaskStatusWorkflow() {
        // 創建多個任務
        TaskResponse task1 = taskService.createTask(new TaskRequest("任務一"));
        TaskResponse task2 = taskService.createTask(new TaskRequest("任務二"));
        
        // 查詢所有任務（應該都是 PENDING）
        List<TaskResponse> allTasks = taskService.getAllTasks(null);
        assertThat(allTasks).hasSize(2);
        assertThat(allTasks).allMatch(task -> "PENDING".equals(task.getStatus()));
        
        // 完成第一個任務
        TaskResponse completedTask = taskService.completeTask(task1.getId());
        assertThat(completedTask.getStatus()).isEqualTo("COMPLETE");
        
        // 按狀態篩選
        List<TaskResponse> pendingTasks = taskService.getAllTasks("PENDING");
        assertThat(pendingTasks).hasSize(1);
        assertThat(pendingTasks.get(0).getTitle()).isEqualTo("任務二");
        
        List<TaskResponse> completedTasks = taskService.getAllTasks("COMPLETE");
        assertThat(completedTasks).hasSize(1);
        assertThat(completedTasks.get(0).getTitle()).isEqualTo("任務一");
    }

    @Test
    @DisplayName("會話生命週期流程：創建會話 -> 查詢活動會話 -> 結束會話並記錄")
    void testSessionLifecycleWorkflow() {
        // 創建任務
        TaskResponse task = taskService.createTask(new TaskRequest("會話測試任務"));
        
        // 創建會話
        SessionRequest sessionRequest = new SessionRequest("測試會話");
        sessionRequest.setTaskId(task.getId());
        sessionRequest.setNote("測試會話流程");
        
        SessionResponse session = sessionService.createSession(sessionRequest);
        assertThat(session).isNotNull();
        assertThat(session.getTitle()).isEqualTo("測試會話");
        
        // 查詢活動會話
        List<SessionResponse> activeSessions = sessionService.getAllSessions();
        assertThat(activeSessions).hasSize(1);
        assertThat(activeSessions.get(0).getId()).isEqualTo(session.getId());
        
        // 結束會話並創建記錄
        SessionRecordCreateRequest endRequest = new SessionRecordCreateRequest();
        endRequest.setSessionId(session.getId());
        endRequest.setCompletionNote("會話順利完成");
        endRequest.setStartTime(LocalDateTime.now().minusHours(1));
        
        SessionRecordResponse record = sessionService.endSession(session.getId(), endRequest);
        assertThat(record).isNotNull();
        assertThat(record.getTitle()).isEqualTo("測試會話");
        assertThat(record.getCompletionNote()).isEqualTo("會話順利完成");
        
        // 驗證會話已被刪除
        List<SessionResponse> remainingSessions = sessionService.getAllSessions();
        assertThat(remainingSessions).isEmpty();
        
        // 驗證記錄已創建
        assertThat(sessionRecordRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("時間範圍查詢流程：創建記錄 -> 時間範圍查詢")
    void testTimeRangeQueryWorkflow() {
        // 創建任務
        TaskResponse task = taskService.createTask(new TaskRequest("時間測試任務"));
        
        // 創建會話並立即結束以產生記錄
        SessionResponse session1 = sessionService.createSession(new SessionRequest("早上會話"));
        SessionRecordCreateRequest record1Request = new SessionRecordCreateRequest();
        record1Request.setSessionId(session1.getId());
        record1Request.setStartTime(LocalDateTime.now().minusHours(3));
        
        SessionRecordResponse record1 = sessionService.endSession(session1.getId(), record1Request);
        
        SessionResponse session2 = sessionService.createSession(new SessionRequest("下午會話"));
        SessionRecordCreateRequest record2Request = new SessionRecordCreateRequest();
        record2Request.setSessionId(session2.getId());
        record2Request.setStartTime(LocalDateTime.now().minusHours(1));
        
        SessionRecordResponse record2 = sessionService.endSession(session2.getId(), record2Request);
        
        // 時間範圍查詢（查詢今天的記錄）
        LocalDate today = LocalDate.now();
        List<SessionRecordResponse> todayRecords = sessionRecordService
                .getSessionRecords(today, today, null);
        
        assertThat(todayRecords).hasSize(2);
        
        // 查詢特定任務的記錄（如果有 taskId 參數）
        List<SessionRecordResponse> taskRecords = sessionRecordService
                .getSessionRecords(null, null, task.getId());
        
        // 由於我們的記錄沒有關聯任務，所以應該為空
        assertThat(taskRecords).isEmpty();
    }

    @Test
    @DisplayName("關聯關係驗證：任務與其他實體的關聯")
    void testEntityRelationships() {
        // 創建標籤
        TagResponse tag = tagService.createTag(new TagRequest("測試標籤", "#123456"));
        
        // 創建任務（關聯標籤）
        TaskRequest taskRequest = new TaskRequest("關聯測試任務");
        taskRequest.setTagIds(List.of(tag.getId()));
        TaskResponse task = taskService.createTask(taskRequest);
        
        // 創建會話（關聯任務）
        SessionRequest sessionRequest = new SessionRequest("關聯測試會話");
        sessionRequest.setTaskId(task.getId());
        SessionResponse session = sessionService.createSession(sessionRequest);
        
        // 結束會話創建記錄（關聯任務）
        SessionRecordCreateRequest recordRequest = new SessionRecordCreateRequest();
        recordRequest.setSessionId(session.getId());
        SessionRecordResponse record = sessionService.endSession(session.getId(), recordRequest);
        
        // 創建時程安排（關聯任務）
        ScheduleEntryRequest scheduleRequest = new ScheduleEntryRequest();
        scheduleRequest.setTitle("關聯測試時程");
        scheduleRequest.setTaskId(task.getId());
        scheduleRequest.setStartAt(LocalDateTime.now().plusHours(1));
        scheduleRequest.setEndAt(LocalDateTime.now().plusHours(2));
        
        ScheduleEntryResponse schedule = scheduleEntryService.createScheduleEntry(scheduleRequest);
        
        // 驗證關聯關係
        assertThat(task.getTags()).hasSize(1);
        assertThat(task.getTags().get(0).getId()).isEqualTo(tag.getId());
        
        assertThat(record.getTaskId()).isEqualTo(task.getId());
        assertThat(schedule.getTaskId()).isEqualTo(task.getId());
        
        // 驗證資料庫中的關聯
        Task taskEntity = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(taskEntity.getTags()).hasSize(1);
        
        SessionRecord recordEntity = sessionRecordRepository.findById(record.getId()).orElseThrow();
        assertThat(recordEntity.getTask()).isNotNull();
        assertThat(recordEntity.getTask().getId()).isEqualTo(task.getId());
    }

    @Test
    @DisplayName("資料驗證流程：無效資料應該被拒絕")
    void testDataValidationWorkflow() {
        // 測試標籤名稱唯一性
        tagService.createTag(new TagRequest("唯一標籤", "#FFFFFF"));
        
        // 嘗試創建同名標籤應該失敗
        assertThatThrownBy(() -> 
                tagService.createTag(new TagRequest("唯一標籤", "#000000")))
                .isInstanceOf(Exception.class);
        
        // 測試無效的顏色格式
        assertThatThrownBy(() -> 
                tagService.createTag(new TagRequest("無效顏色標籤", "invalid-color")))
                .isInstanceOf(Exception.class);
        
        // 測試任務標題不能為空
        assertThatThrownBy(() -> 
                taskService.createTask(new TaskRequest("")))
                .isInstanceOf(Exception.class);
        
        // 測試排程時間驗證
        ScheduleEntryRequest invalidSchedule = new ScheduleEntryRequest();
        invalidSchedule.setTitle("無效時程");
        invalidSchedule.setStartAt(LocalDateTime.now().plusHours(2));
        invalidSchedule.setEndAt(LocalDateTime.now().plusHours(1)); // 結束時間早於開始時間
        
        assertThatThrownBy(() -> 
                scheduleEntryService.createScheduleEntry(invalidSchedule))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("複雜業務流程：多任務並行工作場景")
    void testComplexBusinessWorkflow() {
        // 創建多個標籤
        TagResponse frontendTag = tagService.createTag(new TagRequest("前端", "#3498DB"));
        TagResponse backendTag = tagService.createTag(new TagRequest("後端", "#2ECC71"));
        
        // 創建多個任務
        TaskRequest frontendTaskRequest = new TaskRequest("前端介面開發");
        frontendTaskRequest.setTagIds(List.of(frontendTag.getId()));
        TaskResponse frontendTask = taskService.createTask(frontendTaskRequest);
        
        TaskRequest backendTaskRequest = new TaskRequest("後端API開發");
        backendTaskRequest.setTagIds(List.of(backendTag.getId()));
        TaskResponse backendTask = taskService.createTask(backendTaskRequest);
        
        // 創建多個排程
        ScheduleEntryRequest morningSchedule = new ScheduleEntryRequest();
        morningSchedule.setTitle("前端開發時段");
        morningSchedule.setTaskId(frontendTask.getId());
        morningSchedule.setStartAt(LocalDateTime.now().plusDays(1).withHour(9));
        morningSchedule.setEndAt(LocalDateTime.now().plusDays(1).withHour(12));
        
        ScheduleEntryRequest afternoonSchedule = new ScheduleEntryRequest();
        afternoonSchedule.setTitle("後端開發時段");
        afternoonSchedule.setTaskId(backendTask.getId());
        afternoonSchedule.setStartAt(LocalDateTime.now().plusDays(1).withHour(14));
        afternoonSchedule.setEndAt(LocalDateTime.now().plusDays(1).withHour(17));
        
        ScheduleEntryResponse schedule1 = scheduleEntryService.createScheduleEntry(morningSchedule);
        ScheduleEntryResponse schedule2 = scheduleEntryService.createScheduleEntry(afternoonSchedule);
        
        // 模擬工作執行
        SessionResponse frontendSession = sessionService.createSession(
                new SessionRequest("前端開發會話"));
        SessionResponse backendSession = sessionService.createSession(
                new SessionRequest("後端開發會話"));
        
        // 結束會話並記錄
        SessionRecordCreateRequest frontendRecord = new SessionRecordCreateRequest();
        frontendRecord.setSessionId(frontendSession.getId());
        frontendRecord.setCompletionNote("完成登入頁面組件");
        frontendRecord.setStartTime(LocalDateTime.now().minusHours(3));
        
        SessionRecordCreateRequest backendRecord = new SessionRecordCreateRequest();
        backendRecord.setSessionId(backendSession.getId());
        backendRecord.setCompletionNote("完成用戶認證API");
        backendRecord.setStartTime(LocalDateTime.now().minusHours(2));
        
        SessionRecordResponse record1 = sessionService.endSession(frontendSession.getId(), frontendRecord);
        SessionRecordResponse record2 = sessionService.endSession(backendSession.getId(), backendRecord);
        
        // 完成前端任務
        TaskResponse completedFrontend = taskService.completeTask(frontendTask.getId());
        
        // 驗證最終狀態
        assertThat(completedFrontend.getStatus()).isEqualTo("COMPLETE");
        
        List<TaskResponse> allTasks = taskService.getAllTasks(null);
        long completedCount = allTasks.stream()
                .filter(task -> "COMPLETE".equals(task.getStatus()))
                .count();
        long pendingCount = allTasks.stream()
                .filter(task -> "PENDING".equals(task.getStatus()))
                .count();
        
        assertThat(completedCount).isEqualTo(1);
        assertThat(pendingCount).isEqualTo(1);
        
        // 驗證記錄和排程
        assertThat(sessionRecordRepository.count()).isEqualTo(2);
        assertThat(scheduleEntryRepository.count()).isEqualTo(2);
        assertThat(sessionRepository.count()).isEqualTo(0); // 所有會話都已結束
    }
} 