package com.fcj.problem_suggest.mapper;

import com.fcj.problem_suggest.dto.FigureDto;
import com.fcj.problem_suggest.model.Figure;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface FigureMapper {
    FigureMapper INSTANCE = Mappers.getMapper(FigureMapper.class);

    Figure toEntity(FigureDto object);
    FigureDto toDto(Figure object);
}
