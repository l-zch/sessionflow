package com.sessionflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sessionflow.dto.TagRequest;
import com.sessionflow.dto.TagResponse;
import com.sessionflow.exception.TagNameAlreadyExistsException;
import com.sessionflow.exception.TagNotFoundException;
import com.sessionflow.service.TagService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TagController.class)
@DisplayName("TagController 整合測試")
class TagControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private TagService tagService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @DisplayName("POST /api/tags - 建立標籤成功，回傳 201")
    void createTag_Success() throws Exception {
        // Given
        TagRequest request = new TagRequest("工作", "#FF5733");
        TagResponse response = new TagResponse(1L, "工作", "#FF5733");
        
        when(tagService.createTag(any(TagRequest.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("工作"))
                .andExpect(jsonPath("$.color").value("#FF5733"));
        
        verify(tagService).createTag(any(TagRequest.class));
    }
    
    @Test
    @DisplayName("POST /api/tags - 名稱重複時回傳 409")
    void createTag_NameAlreadyExists_Returns409() throws Exception {
        // Given
        TagRequest request = new TagRequest("工作", "#FF5733");
        
        when(tagService.createTag(any(TagRequest.class)))
                .thenThrow(new TagNameAlreadyExistsException("工作"));
        
        // When & Then
        mockMvc.perform(post("/api/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("TAG_NAME_CONFLICT"))
                .andExpect(jsonPath("$.message").value("標籤名稱重複"))
                .andExpect(jsonPath("$.details").value("Tag with name '工作' already exists"))
                .andExpect(jsonPath("$.timestamp").exists());
        
        verify(tagService).createTag(any(TagRequest.class));
    }
    
    @Test
    @DisplayName("POST /api/tags - 名稱為空時回傳 400")
    void createTag_BlankName_Returns400() throws Exception {
        // Given
        TagRequest request = new TagRequest("", "#FF5733");
        
        // When & Then
        mockMvc.perform(post("/api/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("請求參數驗證失敗"));
        
        verify(tagService, never()).createTag(any());
    }
    
    @Test
    @DisplayName("POST /api/tags - 顏色格式無效時回傳 400")
    void createTag_InvalidColorFormat_Returns400() throws Exception {
        // Given
        TagRequest request = new TagRequest("工作", "invalid-color");
        
        // When & Then
        mockMvc.perform(post("/api/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("請求參數驗證失敗"));
        
        verify(tagService, never()).createTag(any());
    }
    
    @Test
    @DisplayName("POST /api/tags - 缺少必填欄位時回傳 400")
    void createTag_MissingRequiredFields_Returns400() throws Exception {
        // Given - 只有 name，缺少 color
        String incompleteJson = "{\"name\":\"工作\"}";
        
        // When & Then
        mockMvc.perform(post("/api/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(incompleteJson))
                .andExpect(status().isBadRequest());
        
        verify(tagService, never()).createTag(any());
    }
    
    @Test
    @DisplayName("GET /api/tags - 查詢所有標籤成功，回傳 200")
    void getAllTags_Success() throws Exception {
        // Given
        List<TagResponse> responses = List.of(
                new TagResponse(1L, "工作", "#FF5733"),
                new TagResponse(2L, "重要", "#33FF57")
        );
        
        when(tagService.getAllTags()).thenReturn(responses);
        
        // When & Then
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("工作"))
                .andExpect(jsonPath("$[0].color").value("#FF5733"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("重要"))
                .andExpect(jsonPath("$[1].color").value("#33FF57"));
        
        verify(tagService).getAllTags();
    }
    
    @Test
    @DisplayName("GET /api/tags - 空列表時回傳 200")
    void getAllTags_EmptyList_Returns200() throws Exception {
        // Given
        when(tagService.getAllTags()).thenReturn(List.of());
        
        // When & Then
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
        
        verify(tagService).getAllTags();
    }
    
    @Test
    @DisplayName("PUT /api/tags/{id} - 更新標籤成功，回傳 200")
    void updateTag_Success() throws Exception {
        // Given
        Long tagId = 1L;
        TagRequest request = new TagRequest("工作（更新）", "#FF6B35");
        TagResponse response = new TagResponse(tagId, "工作（更新）", "#FF6B35");
        
        when(tagService.updateTag(eq(tagId), any(TagRequest.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(put("/api/tags/{id}", tagId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(tagId))
                .andExpect(jsonPath("$.name").value("工作（更新）"))
                .andExpect(jsonPath("$.color").value("#FF6B35"));
        
        verify(tagService).updateTag(eq(tagId), any(TagRequest.class));
    }
    
    @Test
    @DisplayName("PUT /api/tags/{id} - 標籤不存在時回傳 404")
    void updateTag_NotFound_Returns404() throws Exception {
        // Given
        Long tagId = 999L;
        TagRequest request = new TagRequest("工作", "#FF5733");
        
        when(tagService.updateTag(eq(tagId), any(TagRequest.class)))
                .thenThrow(new TagNotFoundException(tagId));
        
        // When & Then
        mockMvc.perform(put("/api/tags/{id}", tagId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("TAG_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("標籤不存在"))
                .andExpect(jsonPath("$.details").value("Tag with id 999 not found"))
                .andExpect(jsonPath("$.timestamp").exists());
        
        verify(tagService).updateTag(eq(tagId), any(TagRequest.class));
    }
    
    @Test
    @DisplayName("PUT /api/tags/{id} - 名稱重複時回傳 409")
    void updateTag_NameAlreadyExists_Returns409() throws Exception {
        // Given
        Long tagId = 1L;
        TagRequest request = new TagRequest("重要", "#FF5733");
        
        when(tagService.updateTag(eq(tagId), any(TagRequest.class)))
                .thenThrow(new TagNameAlreadyExistsException("重要"));
        
        // When & Then
        mockMvc.perform(put("/api/tags/{id}", tagId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("TAG_NAME_CONFLICT"))
                .andExpect(jsonPath("$.message").value("標籤名稱重複"))
                .andExpect(jsonPath("$.details").value("Tag with name '重要' already exists"));
        
        verify(tagService).updateTag(eq(tagId), any(TagRequest.class));
    }
    
    @Test
    @DisplayName("PUT /api/tags/{id} - 請求參數驗證失敗時回傳 400")
    void updateTag_ValidationError_Returns400() throws Exception {
        // Given
        Long tagId = 1L;
        TagRequest request = new TagRequest("", "invalid-color");
        
        // When & Then
        mockMvc.perform(put("/api/tags/{id}", tagId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("請求參數驗證失敗"));
        
        verify(tagService, never()).updateTag(any(), any());
    }
    
    @Test
    @DisplayName("DELETE /api/tags/{id} - 刪除標籤成功，回傳 204")
    void deleteTag_Success() throws Exception {
        // Given
        Long tagId = 1L;
        
        doNothing().when(tagService).deleteTag(tagId);
        
        // When & Then
        mockMvc.perform(delete("/api/tags/{id}", tagId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        
        verify(tagService).deleteTag(tagId);
    }
    
    @Test
    @DisplayName("DELETE /api/tags/{id} - 標籤不存在時回傳 404")
    void deleteTag_NotFound_Returns404() throws Exception {
        // Given
        Long tagId = 999L;
        
        doThrow(new TagNotFoundException(tagId)).when(tagService).deleteTag(tagId);
        
        // When & Then
        mockMvc.perform(delete("/api/tags/{id}", tagId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("TAG_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("標籤不存在"))
                .andExpect(jsonPath("$.details").value("Tag with id 999 not found"));
        
        verify(tagService).deleteTag(tagId);
    }
    
    @Test
    @DisplayName("POST /api/tags - Content-Type 不正確時回傳 415")
    void createTag_UnsupportedMediaType_Returns415() throws Exception {
        // Given
        TagRequest request = new TagRequest("工作", "#FF5733");
        
        // When & Then
        mockMvc.perform(post("/api/tags")
                .contentType(MediaType.TEXT_PLAIN)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());
        
        verify(tagService, never()).createTag(any());
    }
    
    @Test
    @DisplayName("POST /api/tags - JSON 格式錯誤時回傳 400")
    void createTag_InvalidJson_Returns400() throws Exception {
        // Given
        String invalidJson = "{\"name\":\"工作\",\"color\":}"; // 缺少值
        
        // When & Then
        mockMvc.perform(post("/api/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
        
        verify(tagService, never()).createTag(any());
    }
    
    @Test
    @DisplayName("PUT /api/tags/{id} - 路徑參數為非數字時回傳 400")
    void updateTag_InvalidPathParameter_Returns400() throws Exception {
        // Given
        TagRequest request = new TagRequest("工作", "#FF5733");
        
        // When & Then
        mockMvc.perform(put("/api/tags/{id}", "invalid-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        verify(tagService, never()).updateTag(any(), any());
    }
    
    @Test
    @DisplayName("DELETE /api/tags/{id} - 路徑參數為非數字時回傳 400")
    void deleteTag_InvalidPathParameter_Returns400() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/tags/{id}", "invalid-id"))
                .andExpect(status().isBadRequest());
        
        verify(tagService, never()).deleteTag(any());
    }
    
    @Test
    @DisplayName("POST /api/tags - 極長名稱測試")
    void createTag_VeryLongName_ValidationHandled() throws Exception {
        // Given
        String veryLongName = "a".repeat(1000); // 1000 個字元的名稱
        TagRequest request = new TagRequest(veryLongName, "#FF5733");
        TagResponse response = new TagResponse(1L, veryLongName, "#FF5733");
        
        when(tagService.createTag(any(TagRequest.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(veryLongName));
        
        verify(tagService).createTag(any(TagRequest.class));
    }
    
    @Test
    @DisplayName("POST /api/tags - 特殊字元名稱測試")
    void createTag_SpecialCharactersName_Success() throws Exception {
        // Given
        String specialName = "工作 & 學習 (重要) #1";
        TagRequest request = new TagRequest(specialName, "#FF5733");
        TagResponse response = new TagResponse(1L, specialName, "#FF5733");
        
        when(tagService.createTag(any(TagRequest.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(specialName));
        
        verify(tagService).createTag(any(TagRequest.class));
    }
} 