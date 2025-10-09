package com.fcj.problem_suggest.mapper;

import com.fcj.problem_suggest.dto.FigureDto;
import com.fcj.problem_suggest.model.Figure;
import org.springframework.stereotype.Component;

@Component
public class FigureMapper {
    
    public static final FigureMapper INSTANCE = new FigureMapper();

    public Figure toEntity(FigureDto dto) {
        if (dto == null) {
            return null;
        }
        
        Figure entity = new Figure();
        entity.setId(dto.getId());
        entity.setMetadata(dto.getMetadata());
        // Note: Statement reference should be set by the service layer
        return entity;
    }

    public FigureDto toDto(Figure entity) {
        if (entity == null) {
            return null;
        }
        
        FigureDto dto = new FigureDto();
        dto.setId(entity.getId());
        dto.setStatementId(entity.getStatement() != null ? entity.getStatement().getId() : null);
        dto.setMetadata(entity.getMetadata());
        return dto;
    }
}