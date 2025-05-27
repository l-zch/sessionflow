package com.sessionflow.service.impl;

import com.sessionflow.dto.TagRequest;
import com.sessionflow.dto.TagResponse;
import com.sessionflow.exception.TagNameAlreadyExistsException;
import com.sessionflow.exception.TagNotFoundException;
import com.sessionflow.mapper.TagMapper;
import com.sessionflow.model.Tag;
import com.sessionflow.repository.TagRepository;
import com.sessionflow.service.application.impl.TagServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagService 單元測試")
class TagServiceImplTest {
    
    @Mock
    private TagRepository tagRepository;
    
    @Mock
    private TagMapper tagMapper;
    
    @InjectMocks
    private TagServiceImpl tagService;
    
    private TagRequest tagRequest;
    private Tag tag;
    private TagResponse tagResponse;
    
    @BeforeEach
    void setUp() {
        tagRequest = new TagRequest("工作", "#FF5733");
        tag = new Tag("工作", "#FF5733");
        tag.setId(1L);
        tagResponse = new TagResponse(1L, "工作", "#FF5733");
    }
    
    @Test
    @DisplayName("成功建立標籤")
    void createTag_Success() {
        // Given
        when(tagRepository.existsByName("工作")).thenReturn(false);
        when(tagMapper.toEntity(tagRequest)).thenReturn(tag);
        when(tagRepository.save(tag)).thenReturn(tag);
        when(tagMapper.toResponse(tag)).thenReturn(tagResponse);
        
        // When
        TagResponse result = tagService.createTag(tagRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("工作");
        assertThat(result.getColor()).isEqualTo("#FF5733");
        
        verify(tagRepository).existsByName("工作");
        verify(tagMapper).toEntity(tagRequest);
        verify(tagRepository).save(tag);
        verify(tagMapper).toResponse(tag);
    }
    
    @Test
    @DisplayName("建立標籤時名稱重複，應丟出 TagNameAlreadyExistsException")
    void createTag_NameAlreadyExists_ThrowsException() {
        // Given
        when(tagRepository.existsByName("工作")).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> tagService.createTag(tagRequest))
                .isInstanceOf(TagNameAlreadyExistsException.class)
                .hasMessage("Tag with name '工作' already exists");
        
        verify(tagRepository).existsByName("工作");
        verify(tagMapper, never()).toEntity(any());
        verify(tagRepository, never()).save(any());
        verify(tagMapper, never()).toResponse(any());
    }
    
    @Test
    @DisplayName("查詢所有標籤成功")
    void getAllTags_Success() {
        // Given
        Tag tag2 = new Tag("重要", "#33FF57");
        tag2.setId(2L);
        List<Tag> tags = List.of(tag, tag2);
        
        TagResponse tagResponse2 = new TagResponse(2L, "重要", "#33FF57");
        List<TagResponse> expectedResponses = List.of(tagResponse, tagResponse2);
        
        when(tagRepository.findAll()).thenReturn(tags);
        when(tagMapper.toResponseList(tags)).thenReturn(expectedResponses);
        
        // When
        List<TagResponse> result = tagService.getAllTags();
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("工作");
        assertThat(result.get(1).getName()).isEqualTo("重要");
        
        verify(tagRepository).findAll();
        verify(tagMapper).toResponseList(tags);
    }
    
    @Test
    @DisplayName("查詢所有標籤成功 - 空列表")
    void getAllTags_EmptyList() {
        // Given
        List<Tag> emptyTags = List.of();
        List<TagResponse> emptyResponses = List.of();
        
        when(tagRepository.findAll()).thenReturn(emptyTags);
        when(tagMapper.toResponseList(emptyTags)).thenReturn(emptyResponses);
        
        // When
        List<TagResponse> result = tagService.getAllTags();
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        
        verify(tagRepository).findAll();
        verify(tagMapper).toResponseList(emptyTags);
    }
    
    @Test
    @DisplayName("更新標籤成功")
    void updateTag_Success() {
        // Given
        Long tagId = 1L;
        TagRequest updateRequest = new TagRequest("工作（更新）", "#FF6B35");
        Tag updatedTag = new Tag("工作（更新）", "#FF6B35");
        updatedTag.setId(tagId);
        TagResponse updatedResponse = new TagResponse(tagId, "工作（更新）", "#FF6B35");
        
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));
        when(tagRepository.existsByNameAndIdNot("工作（更新）", tagId)).thenReturn(false);
        when(tagRepository.save(tag)).thenReturn(updatedTag);
        when(tagMapper.toResponse(updatedTag)).thenReturn(updatedResponse);
        
        // When
        TagResponse result = tagService.updateTag(tagId, updateRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(tagId);
        assertThat(result.getName()).isEqualTo("工作（更新）");
        assertThat(result.getColor()).isEqualTo("#FF6B35");
        
        verify(tagRepository).findById(tagId);
        verify(tagRepository).existsByNameAndIdNot("工作（更新）", tagId);
        verify(tagMapper).updateEntityFromRequest(tag, updateRequest);
        verify(tagRepository).save(tag);
        verify(tagMapper).toResponse(updatedTag);
    }
    
    @Test
    @DisplayName("更新標籤時 ID 不存在，應丟出 TagNotFoundException")
    void updateTag_TagNotFound_ThrowsException() {
        // Given
        Long nonExistentId = 999L;
        TagRequest updateRequest = new TagRequest("工作（更新）", "#FF6B35");
        
        when(tagRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> tagService.updateTag(nonExistentId, updateRequest))
                .isInstanceOf(TagNotFoundException.class)
                .hasMessage("Tag with id 999 not found");
        
        verify(tagRepository).findById(nonExistentId);
        verify(tagRepository, never()).existsByNameAndIdNot(any(), any());
        verify(tagMapper, never()).updateEntityFromRequest(any(), any());
        verify(tagRepository, never()).save(any());
        verify(tagMapper, never()).toResponse(any());
    }
    
    @Test
    @DisplayName("更新標籤時名稱重複，應丟出 TagNameAlreadyExistsException")
    void updateTag_NameAlreadyExists_ThrowsException() {
        // Given
        Long tagId = 1L;
        TagRequest updateRequest = new TagRequest("重要", "#FF6B35");
        
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));
        when(tagRepository.existsByNameAndIdNot("重要", tagId)).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> tagService.updateTag(tagId, updateRequest))
                .isInstanceOf(TagNameAlreadyExistsException.class)
                .hasMessage("Tag with name '重要' already exists");
        
        verify(tagRepository).findById(tagId);
        verify(tagRepository).existsByNameAndIdNot("重要", tagId);
        verify(tagMapper, never()).updateEntityFromRequest(any(), any());
        verify(tagRepository, never()).save(any());
        verify(tagMapper, never()).toResponse(any());
    }
    
    @Test
    @DisplayName("更新標籤時名稱相同（自己的名稱），應成功更新")
    void updateTag_SameName_Success() {
        // Given
        Long tagId = 1L;
        TagRequest updateRequest = new TagRequest("工作", "#FF6B35"); // 相同名稱，不同顏色
        Tag updatedTag = new Tag("工作", "#FF6B35");
        updatedTag.setId(tagId);
        TagResponse updatedResponse = new TagResponse(tagId, "工作", "#FF6B35");
        
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));
        when(tagRepository.existsByNameAndIdNot("工作", tagId)).thenReturn(false);
        when(tagRepository.save(tag)).thenReturn(updatedTag);
        when(tagMapper.toResponse(updatedTag)).thenReturn(updatedResponse);
        
        // When
        TagResponse result = tagService.updateTag(tagId, updateRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(tagId);
        assertThat(result.getName()).isEqualTo("工作");
        assertThat(result.getColor()).isEqualTo("#FF6B35");
        
        verify(tagRepository).findById(tagId);
        verify(tagRepository).existsByNameAndIdNot("工作", tagId);
        verify(tagMapper).updateEntityFromRequest(tag, updateRequest);
        verify(tagRepository).save(tag);
        verify(tagMapper).toResponse(updatedTag);
    }
    
    @Test
    @DisplayName("刪除標籤成功")
    void deleteTag_Success() {
        // Given
        Long tagId = 1L;
        
        when(tagRepository.existsById(tagId)).thenReturn(true);
        doNothing().when(tagRepository).deleteById(tagId);
        
        // When
        assertThatCode(() -> tagService.deleteTag(tagId))
                .doesNotThrowAnyException();
        
        // Then
        verify(tagRepository).existsById(tagId);
        verify(tagRepository).deleteById(tagId);
    }
    
    @Test
    @DisplayName("刪除不存在的標籤 ID，應丟出 TagNotFoundException")
    void deleteTag_TagNotFound_ThrowsException() {
        // Given
        Long nonExistentId = 999L;
        
        when(tagRepository.existsById(nonExistentId)).thenReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> tagService.deleteTag(nonExistentId))
                .isInstanceOf(TagNotFoundException.class)
                .hasMessage("Tag with id 999 not found");
        
        verify(tagRepository).existsById(nonExistentId);
        verify(tagRepository, never()).deleteById(any());
    }
    
    @Test
    @DisplayName("建立標籤時使用空白名稱 - 應由 validation 處理")
    void createTag_BlankName_ValidationHandled() {
        // Given - 這個測試主要驗證 service 層的行為
        // 實際的 validation 會在 controller 層處理
        TagRequest blankNameRequest = new TagRequest("", "#FF5733");
        
        when(tagRepository.existsByName("")).thenReturn(false);
        when(tagMapper.toEntity(blankNameRequest)).thenReturn(new Tag("", "#FF5733"));
        when(tagRepository.save(any())).thenReturn(tag);
        when(tagMapper.toResponse(any())).thenReturn(tagResponse);
        
        // When & Then - service 層不會阻止空白名稱，這由 validation 處理
        assertThatCode(() -> tagService.createTag(blankNameRequest))
                .doesNotThrowAnyException();
        
        verify(tagRepository).existsByName("");
    }
    
    @Test
    @DisplayName("建立標籤時使用無效顏色格式 - 應由 validation 處理")
    void createTag_InvalidColorFormat_ValidationHandled() {
        // Given - 這個測試主要驗證 service 層的行為
        // 實際的 validation 會在 controller 層處理
        TagRequest invalidColorRequest = new TagRequest("工作", "invalid-color");
        
        when(tagRepository.existsByName("工作")).thenReturn(false);
        when(tagMapper.toEntity(invalidColorRequest)).thenReturn(new Tag("工作", "invalid-color"));
        when(tagRepository.save(any())).thenReturn(tag);
        when(tagMapper.toResponse(any())).thenReturn(tagResponse);
        
        // When & Then - service 層不會阻止無效顏色，這由 validation 處理
        assertThatCode(() -> tagService.createTag(invalidColorRequest))
                .doesNotThrowAnyException();
        
        verify(tagRepository).existsByName("工作");
    }
} 