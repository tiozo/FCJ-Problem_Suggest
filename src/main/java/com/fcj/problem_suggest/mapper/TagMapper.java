package com.fcj.problem_suggest.mapper;

import com.fcj.problem_suggest.dto.TagDto;
import com.fcj.problem_suggest.model.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TagMapper {
    TagMapper INSTANCE = Mappers.getMapper(TagMapper.class);

    Tag toEntity(TagDto object);
    TagDto toDto(Tag object);
}
