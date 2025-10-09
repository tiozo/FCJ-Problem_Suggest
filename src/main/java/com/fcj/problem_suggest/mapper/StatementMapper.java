package com.fcj.problem_suggest.mapper;

import com.fcj.problem_suggest.dto.StatementDto;
import com.fcj.problem_suggest.model.Statement;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {FigureMapper.class, TagMapper.class})
public interface StatementMapper {
    StatementMapper INSTANCE = Mappers.getMapper(StatementMapper.class);

    Statement toEntity(StatementDto object);
    StatementDto toDto(Statement object);
}
