package com.fcj.problem_suggest.mapper;

import com.fcj.problem_suggest.dto.StatementDto;
import com.fcj.problem_suggest.model.Statement;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StatementMapperTest {

    @Test
    void toEntity_shouldMapAllFields() {
        // Given
        UUID id = UUID.randomUUID();
        StatementDto dto = new StatementDto();
        dto.setId(id);
        dto.setText("What is 2+2?");
        dto.setAnswer("A. 4\nB. 5");

        // When
        Statement entity = StatementMapper.INSTANCE.toEntity(dto);

        // Then
        assertNotNull(entity);
        assertEquals(id, entity.getId());
        assertEquals("What is 2+2?", entity.getText());
        assertEquals("A. 4\nB. 5", entity.getAnswer());
    }

    @Test
    void toEntity_shouldHandleNullAnswer() {
        // Given
        UUID id = UUID.randomUUID();
        StatementDto dto = new StatementDto();
        dto.setId(id);
        dto.setText("What is 2+2?");
        dto.setAnswer(null);

        // When
        Statement entity = StatementMapper.INSTANCE.toEntity(dto);

        // Then
        assertNotNull(entity);
        assertEquals(id, entity.getId());
        assertEquals("What is 2+2?", entity.getText());
        assertNull(entity.getAnswer());
    }

    @Test
    void toDto_shouldMapAllFields() {
        // Given
        UUID id = UUID.randomUUID();
        Statement entity = new Statement();
        entity.setId(id);
        entity.setText("What is 2+2?");
        entity.setAnswer("A. 4\nB. 5");

        // When
        StatementDto dto = StatementMapper.INSTANCE.toDto(entity);

        // Then
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals("What is 2+2?", dto.getText());
        assertEquals("A. 4\nB. 5", dto.getAnswer());
    }

    @Test
    void roundTrip_shouldPreserveData() {
        // Given
        UUID id = UUID.randomUUID();
        StatementDto originalDto = new StatementDto();
        originalDto.setId(id);
        originalDto.setText("What is the capital of France?");
        originalDto.setAnswer("A. London\nB. Paris\nC. Berlin");

        // When
        Statement entity = StatementMapper.INSTANCE.toEntity(originalDto);
        StatementDto resultDto = StatementMapper.INSTANCE.toDto(entity);

        // Then
        assertEquals(originalDto.getId(), resultDto.getId());
        assertEquals(originalDto.getText(), resultDto.getText());
        assertEquals(originalDto.getAnswer(), resultDto.getAnswer());
    }
}