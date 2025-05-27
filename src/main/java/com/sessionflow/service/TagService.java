package com.sessionflow.service;

import com.sessionflow.dto.TagRequest;
import com.sessionflow.dto.TagResponse;

import java.util.List;

public interface TagService {
    
    /**
     * 建立新標籤
     * @param request 標籤建立請求
     * @return 建立的標籤回應
     * @throws com.sessionflow.exception.TagNameAlreadyExistsException 當標籤名稱已存在時
     */
    TagResponse createTag(TagRequest request);
    
    /**
     * 查詢所有標籤
     * @return 所有標籤列表
     */
    List<TagResponse> getAllTags();
    
    /**
     * 更新標籤
     * @param id 標籤 ID
     * @param request 標籤更新請求
     * @return 更新後的標籤回應
     * @throws com.sessionflow.exception.TagNotFoundException 當標籤不存在時
     * @throws com.sessionflow.exception.TagNameAlreadyExistsException 當標籤名稱已存在時
     */
    TagResponse updateTag(Long id, TagRequest request);
    
    /**
     * 刪除標籤
     * @param id 標籤 ID
     * @throws com.sessionflow.exception.TagNotFoundException 當標籤不存在時
     */
    void deleteTag(Long id);
} 