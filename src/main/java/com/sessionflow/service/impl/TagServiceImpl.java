package com.sessionflow.service.impl;

import com.sessionflow.dto.TagRequest;
import com.sessionflow.dto.TagResponse;
import com.sessionflow.exception.TagNameAlreadyExistsException;
import com.sessionflow.exception.TagNotFoundException;
import com.sessionflow.mapper.TagMapper;
import com.sessionflow.model.Tag;
import com.sessionflow.repository.TagRepository;
import com.sessionflow.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TagServiceImpl implements TagService {
    
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;
    
    @Override
    public TagResponse createTag(TagRequest request) {
        log.info("Creating tag with name: {}", request.getName());
        
        // 檢查名稱是否已存在
        if (tagRepository.existsByName(request.getName())) {
            log.warn("Tag name already exists: {}", request.getName());
            throw new TagNameAlreadyExistsException(request.getName());
        }
        
        // 建立並儲存標籤
        Tag tag = tagMapper.toEntity(request);
        Tag savedTag = tagRepository.save(tag);
        
        log.info("Tag created successfully with id: {}", savedTag.getId());
        return tagMapper.toResponse(savedTag);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getAllTags() {
        log.info("Fetching all tags");
        
        List<Tag> tags = tagRepository.findAll();
        
        log.info("Found {} tags", tags.size());
        return tagMapper.toResponseList(tags);
    }
    
    @Override
    public TagResponse updateTag(Long id, TagRequest request) {
        log.info("Updating tag with id: {}", id);
        
        // 查詢標籤是否存在
        Tag existingTag = tagRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Tag not found with id: {}", id);
                    return new TagNotFoundException(id);
                });
        
        // 檢查名稱是否與其他標籤重複
        if (tagRepository.existsByNameAndIdNot(request.getName(), id)) {
            log.warn("Tag name already exists: {}", request.getName());
            throw new TagNameAlreadyExistsException(request.getName());
        }
        
        // 更新標籤
        tagMapper.updateEntityFromRequest(existingTag, request);
        Tag updatedTag = tagRepository.save(existingTag);
        
        log.info("Tag updated successfully with id: {}", updatedTag.getId());
        return tagMapper.toResponse(updatedTag);
    }
    
    @Override
    public void deleteTag(Long id) {
        log.info("Deleting tag with id: {}", id);
        
        // 檢查標籤是否存在
        if (!tagRepository.existsById(id)) {
            log.warn("Tag not found with id: {}", id);
            throw new TagNotFoundException(id);
        }
        
        tagRepository.deleteById(id);
        log.info("Tag deleted successfully with id: {}", id);
    }
} 