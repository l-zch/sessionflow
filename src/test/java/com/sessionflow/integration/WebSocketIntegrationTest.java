package com.sessionflow.integration;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sessionflow.dto.TaskRequest;
import com.sessionflow.dto.TaskResponse;
import com.sessionflow.dto.ResourceChangedNotification;
import com.sessionflow.model.Session;
import com.sessionflow.model.SessionRecord;
import com.sessionflow.model.ScheduleEntry;
import com.sessionflow.model.Task;
import com.sessionflow.repository.ScheduleEntryRepository;
import com.sessionflow.repository.SessionRecordRepository;
import com.sessionflow.repository.SessionRepository;
import com.sessionflow.repository.TaskRepository;
import com.sessionflow.service.TaskService;

import lombok.extern.slf4j.Slf4j;
import static org.assertj.core.api.Assertions.*;

@SuppressWarnings({ "unused", "null" })

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("WebSocket 整合測試")
@Slf4j
class WebSocketIntegrationTest {
    

    @LocalServerPort
    private int port;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private SessionRepository sessionRepository;
    
    @Autowired
    private SessionRecordRepository sessionRecordRepository;
    
    @Autowired
    private ScheduleEntryRepository scheduleEntryRepository;
    
    private WebSocketStompClient stompClient;
    private StompSession stompSession;
    private BlockingQueue<ResourceChangedNotification<?>> receivedNotifications;
    private ObjectMapper objectMapper;
    private CompletableFuture<StompSession> sessionFuture;
    
    @BeforeEach
    void setUp() throws Exception {
        receivedNotifications = new LinkedBlockingQueue<>();
        objectMapper = new ObjectMapper();
        
        // 使用 SockJS 客戶端
        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);
        
        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        
        // 使用 SockJS URL 格式
        String url = "http://localhost:" + port + "/ws";
        
        sessionFuture = stompClient.connectAsync(url, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                System.out.println("WebSocket 連接成功: " + session.getSessionId());
            }
            
            @Override
            public void handleException(StompSession session, StompCommand command, 
                                      StompHeaders headers, byte[] payload, Throwable exception) {
                System.err.println("WebSocket 異常: " + exception.getMessage());
                exception.printStackTrace();
            }
            
            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                System.err.println("WebSocket 傳輸錯誤: " + exception.getMessage());
                exception.printStackTrace();
            }
        });
        
        // 等待連接建立，增加超時時間
        stompSession = sessionFuture.get(10, TimeUnit.SECONDS);
        
        // 訂閱資料更新通知
        stompSession.subscribe("/topic/notification", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }
            
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> payloadMap = (Map<String, Object>) payload;
                    
                    // 詳細日誌 JSON 內容
                    System.out.println("收到 JSON: " + payloadMap);
                    System.out.println("包含的欄位: " + payloadMap.keySet());
                    
                    // 根據是否包含 primaryResourceType 判斷通知類型
                    ResourceChangedNotification<?> notification;
                    
                    System.out.println("解析資源更新通知");
                    notification = objectMapper.convertValue(payloadMap, ResourceChangedNotification.class);
                    
                    receivedNotifications.offer(notification);
                    System.out.println("收到通知: " + notification);
                } catch (Exception e) {
                    System.err.println("處理通知時發生錯誤: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        
        // 等待訂閱完成
        Thread.sleep(500);
    }
    
    @AfterEach
    void tearDown() {
        if (stompSession != null && stompSession.isConnected()) {
            stompSession.disconnect();
        }
        if (stompClient != null) {
            stompClient.stop();
        }
    }
    
    @Test
    @DisplayName("WebSocket 連接成功")
    void webSocketConnection_Success() {
        assertThat(stompSession).isNotNull();
        assertThat(stompSession.isConnected()).isTrue();
    }
    
    @Test
    @DisplayName("任務建立時發送 WebSocket 通知")
    void taskCreate_SendsWebSocketNotification() throws Exception {
        // Given
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setTitle("測試任務");
        taskRequest.setNote("測試任務描述");
        
        // When
        TaskResponse createdTask = taskService.createTask(taskRequest);
        
        // Then
        // 等待 WebSocket 通知
        ResourceChangedNotification<?> notification = receivedNotifications.poll(5, TimeUnit.SECONDS);
        
        assertThat(notification).isNotNull();
        assertThat(notification.notificationType().toValue()).isEqualTo("task_create");
        assertThat(notification.id()).isEqualTo(createdTask.getId());
        assertThat(notification.data()).isNotNull();
        
        log.info("收到任務建立通知: {}", notification);
    }
    
    @Test
    @DisplayName("任務級聯刪除時發送包含 affected 的 WebSocket 通知")
    void taskCascadeDelete_SendsWebSocketNotificationWithAffected() throws Exception {
        // Given - 建立任務
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setTitle("測試級聯刪除任務");
        taskRequest.setNote("這個任務會有相關的 Session 和 SessionRecord");
        TaskResponse createdTask = taskService.createTask(taskRequest);
        
        // 等待並消費任務建立通知
        ResourceChangedNotification<?> createNotification = receivedNotifications.poll(5, TimeUnit.SECONDS);
        assertThat(createNotification).isNotNull();
        assertThat(createNotification.notificationType().toValue()).isEqualTo("task_create");
        log.info("已消費任務建立通知: {}", createNotification.notificationType());
        
        // 建立相關的 Session 和 SessionRecord（直接使用 Repository 建立測試資料）
        Task task = taskRepository.findById(createdTask.getId()).orElseThrow();
        
        // 建立 Session - 使用有參建構子確保 startAt 正確設定
        Session session1 = new Session("測試 Session 1");
        session1.setNote("Session 1 備註");
        session1.setTask(task);
        Session savedSession1 = sessionRepository.save(session1);
        
        Session session2 = new Session("測試 Session 2");
        session2.setNote("Session 2 備註");
        session2.setTask(task);
        Session savedSession2 = sessionRepository.save(session2);
        
        // 建立 SessionRecord
        SessionRecord record1 = new SessionRecord();
        record1.setTitle("測試 SessionRecord 1");
        record1.setPlannedNote("計劃備註 1");
        record1.setCompletionNote("完成備註 1");
        record1.setStartAt(java.time.LocalDateTime.now().minusHours(2));
        record1.setEndAt(java.time.LocalDateTime.now().minusHours(1));
        record1.setTask(task);
        SessionRecord savedRecord1 = sessionRecordRepository.save(record1);
        
        // 建立 ScheduleEntry
        ScheduleEntry schedule1 = new ScheduleEntry();
        schedule1.setTitle("測試 ScheduleEntry 1");
        schedule1.setNote("排程備註 1");
        schedule1.setStartAt(java.time.LocalDateTime.now().plusHours(1));
        schedule1.setEndAt(java.time.LocalDateTime.now().plusHours(2));
        schedule1.setTask(task);
        ScheduleEntry savedSchedule1 = scheduleEntryRepository.save(schedule1);
        
        log.info("建立測試資料 - Task: {}, Session: [{}, {}], SessionRecord: [{}], ScheduleEntry: [{}]", 
                createdTask.getId(), savedSession1.getId(), savedSession2.getId(), 
                savedRecord1.getId(), savedSchedule1.getId());
        
        // When - 刪除任務（觸發級聯刪除）
        taskService.deleteTask(createdTask.getId());
        
        // Then - 等待 WebSocket 刪除通知
        ResourceChangedNotification<?> deleteNotification = receivedNotifications.poll(5, TimeUnit.SECONDS);
        
        assertThat(deleteNotification).isNotNull();
        assertThat(deleteNotification.notificationType().toValue()).isEqualTo("task_delete");
        assertThat(deleteNotification.id()).isEqualTo(createdTask.getId());
        assertThat(deleteNotification.data()).isNull(); // 刪除操作不包含 data
        
        // 驗證 affected 包含級聯刪除的資源
        assertThat(deleteNotification.affected()).isNotNull();
        assertThat(deleteNotification.affected()).hasSize(3); // Session, SessionRecord, ScheduleEntry
        
        // 驗證 Session 刪除
        var sessionAffected = deleteNotification.affected().stream()
                .filter(a -> a.notificationType().toValue().equals("session_delete"))
                .findFirst();
        assertThat(sessionAffected).isPresent();
        assertThat(sessionAffected.get().ids()).containsExactlyInAnyOrder(
                savedSession1.getId(), savedSession2.getId());
        
        // 驗證 SessionRecord 刪除
        var sessionRecordAffected = deleteNotification.affected().stream()
                .filter(a -> a.notificationType().toValue().equals("session_record_delete"))
                .findFirst();
        assertThat(sessionRecordAffected).isPresent();
        assertThat(sessionRecordAffected.get().ids()).containsExactly(savedRecord1.getId());
        
        // 驗證 ScheduleEntry 刪除
        var scheduleEntryAffected = deleteNotification.affected().stream()
                .filter(a -> a.notificationType().toValue().equals("schedule_entry_delete"))
                .findFirst();
        assertThat(scheduleEntryAffected).isPresent();
        assertThat(scheduleEntryAffected.get().ids()).containsExactly(savedSchedule1.getId());
        
        log.info("收到任務級聯刪除通知: {}", deleteNotification);
        log.info("Affected 資源: {}", deleteNotification.affected());
        
        // 驗證資料確實被刪除
        assertThat(taskRepository.findById(createdTask.getId())).isEmpty();
        assertThat(sessionRepository.findById(savedSession1.getId())).isEmpty();
        assertThat(sessionRepository.findById(savedSession2.getId())).isEmpty();
        assertThat(sessionRecordRepository.findById(savedRecord1.getId())).isEmpty();
        assertThat(scheduleEntryRepository.findById(savedSchedule1.getId())).isEmpty();
    }
} 