package com.sessionflow.mapper.impl;

import com.sessionflow.dto.TagRequest;
import com.sessionflow.dto.TagResponse;
import com.sessionflow.model.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagMapper 單元測試")
class TagMapperImplTest {

    @InjectMocks
    private TagMapperImpl tagMapper;

    private TagRequest tagRequest;
    private Tag tag;

    @BeforeEach
    void setUp() {
        // 建立測試用的 TagRequest
        tagRequest = new TagRequest();
        tagRequest.setName("工作");
        tagRequest.setColor("#FF0000");

        // 建立測試用的 Tag
        tag = new Tag("工作", "#FF0000");
        tag.setId(1L);
    }

    @Test
    @DisplayName("TagRequest 轉換為 Tag 實體成功")
    void toEntity_ValidTagRequest_Success() {
        // When
        Tag result = tagMapper.toEntity(tagRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("工作");
        assertThat(result.getColor()).isEqualTo("#FF0000");
        assertThat(result.getId()).isNull(); // 新建實體 ID 為 null
    }

    @Test
    @DisplayName("TagRequest 為 null 時返回 null")
    void toEntity_NullRequest_ReturnsNull() {
        // When
        Tag result = tagMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Tag 實體轉換為 TagResponse 成功")
    void toResponse_ValidTag_Success() {
        // When
        TagResponse result = tagMapper.toResponse(tag);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("工作");
        assertThat(result.getColor()).isEqualTo("#FF0000");
    }

    @Test
    @DisplayName("Tag 為 null 時返回 null")
    void toResponse_NullTag_ReturnsNull() {
        // When
        TagResponse result = tagMapper.toResponse(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Tag 列表轉換為 TagResponse 列表成功")
    void toResponseList_ValidTagList_Success() {
        // Given
        Tag tag2 = new Tag("學習", "#00FF00");
        tag2.setId(2L);

        List<Tag> tags = List.of(tag, tag2);

        // When
        List<TagResponse> result = tagMapper.toResponseList(tags);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("工作");
        assertThat(result.get(0).getColor()).isEqualTo("#FF0000");
        
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getName()).isEqualTo("學習");
        assertThat(result.get(1).getColor()).isEqualTo("#00FF00");
    }

    @Test
    @DisplayName("空 Tag 列表轉換為空 TagResponse 列表")
    void toResponseList_EmptyTagList_ReturnsEmptyList() {
        // Given
        List<Tag> emptyTags = List.of();

        // When
        List<TagResponse> result = tagMapper.toResponseList(emptyTags);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }
} 