package com.fcj.problem_suggest.mapper;

import com.fcj.problem_suggest.dto.TagDto;
import com.fcj.problem_suggest.model.Tag;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {
    
    public static final TagMapper INSTANCE = new TagMapper();

    public Tag toEntity(TagDto dto) {
        if (dto == null) {
            return null;
        }
        
        Tag entity = new Tag();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        // Note: statements relationship handled separately to avoid circular references
        return entity;
    }

    public TagDto toDto(Tag entity) {
        if (entity == null) {
            return null;
        }
        
        TagDto dto = new TagDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        // Note: statements relationship handled separately to avoid circular references
        return dto;
    }
}