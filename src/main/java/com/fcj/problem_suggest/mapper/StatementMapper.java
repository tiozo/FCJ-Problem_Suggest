package com.fcj.problem_suggest.mapper;

import com.fcj.problem_suggest.dto.StatementDto;
import com.fcj.problem_suggest.model.Statement;
import org.springframework.stereotype.Component;

@Component
public class StatementMapper {
    
    public static final StatementMapper INSTANCE = new StatementMapper();

    public Statement toEntity(StatementDto dto) {
        if (dto == null) {
            return null;
        }
        
        Statement entity = new Statement();
        entity.setId(dto.getId());
        entity.setText(dto.getText());
        entity.setAnswer(dto.getAnswer());
        return entity;
    }

    public StatementDto toDto(Statement entity) {
        if (entity == null) {
            return null;
        }
        
        StatementDto dto = new StatementDto();
        dto.setId(entity.getId());
        dto.setText(entity.getText());
        dto.setAnswer(entity.getAnswer());
        return dto;
    }
}