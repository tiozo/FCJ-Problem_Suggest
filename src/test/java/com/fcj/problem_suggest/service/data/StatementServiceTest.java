package com.fcj.problem_suggest.service.data;

import com.fcj.problem_suggest.dto.StatementDto;
import com.fcj.problem_suggest.model.Statement;
import com.fcj.problem_suggest.repository.StatementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StatementServiceTest {

    @Mock
    private StatementRepository repository;

    private StatementService service;
    private UUID testId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new StatementService(repository);
        testId = UUID.randomUUID();
    }

    @Test
    void save_shouldReturnSavedDto() {
        StatementDto dto = new StatementDto(testId, "Test", "Answer", null, new HashSet<>());
        Statement entity = new Statement();
        entity.setId(testId);
        entity.setText("Test");
        entity.setAnswer("Answer");

        when(repository.save(any(Statement.class))).thenReturn(entity);

        StatementDto result = service.save(dto);

        assertNotNull(result);
        verify(repository).save(any(Statement.class));
    }

    @Test
    void findById_shouldReturnDto() {
        Statement entity = new Statement();
        entity.setId(testId);
        entity.setText("Test");
        entity.setAnswer("Answer");

        when(repository.findById(testId)).thenReturn(Optional.of(entity));

        Optional<StatementDto> result = service.findById(testId);

        assertTrue(result.isPresent());
        verify(repository).findById(testId);
    }

    @Test
    void deleteById_shouldCallRepository() {
        service.deleteById(testId);
        verify(repository).deleteById(testId);
    }
}