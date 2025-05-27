package com.sessionflow.mapper;

import com.sessionflow.dto.TagRequest;
import com.sessionflow.dto.TagResponse;
import com.sessionflow.model.Tag;

import java.util.List;

public interface TagMapper {
    
    /**
     * 將 TagRequest 轉換為 Tag 實體
     * @param request TagRequest
     * @return Tag 實體
     */
    Tag toEntity(TagRequest request);
    
    /**
     * 將 Tag 實體轉換為 TagResponse
     * @param tag Tag 實體
     * @return TagResponse
     */
    TagResponse toResponse(Tag tag);
    
    /**
     * 將 Tag 實體列表轉換為 TagResponse 列表
     * @param tags Tag 實體列表
     * @return TagResponse 列表
     */
    List<TagResponse> toResponseList(List<Tag> tags);
    
    /**
     * 更新 Tag 實體的欄位（從 TagRequest）
     * @param tag 要更新的 Tag 實體
     * @param request TagRequest
     */
    void updateEntityFromRequest(Tag tag, TagRequest request);
} 