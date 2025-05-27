package com.sessionflow.mapper.impl;

import com.sessionflow.dto.TagRequest;
import com.sessionflow.dto.TagResponse;
import com.sessionflow.mapper.TagMapper;
import com.sessionflow.model.Tag;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TagMapperImpl implements TagMapper {
    
    @Override
    public Tag toEntity(TagRequest request) {
        if (request == null) {
            return null;
        }
        
        return new Tag(request.getName(), request.getColor());
    }
    
    @Override
    public TagResponse toResponse(Tag tag) {
        if (tag == null) {
            return null;
        }
        
        return new TagResponse(
            tag.getId(),
            tag.getName(),
            tag.getColor()
        );
    }
    
    @Override
    public List<TagResponse> toResponseList(List<Tag> tags) {
        if (tags == null) {
            return null;
        }
        
        return tags.stream()
                .map(this::toResponse)
                .toList();
    }
    
    @Override
    public void updateEntityFromRequest(Tag tag, TagRequest request) {
        if (tag == null || request == null) {
            return;
        }
        
        tag.setName(request.getName());
        tag.setColor(request.getColor());
    }
} 