package com.fcj.problem_suggest.service.data;

import com.fcj.problem_suggest.dto.StatementDto;
import com.fcj.problem_suggest.mapper.StatementMapper;
import com.fcj.problem_suggest.model.Statement;
import com.fcj.problem_suggest.repository.StatementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StatementService {
    
    private final StatementRepository repository;
    
    public StatementDto save(StatementDto dto) {
        Statement entity = StatementMapper.INSTANCE.toEntity(dto);
        Statement saved = repository.save(entity);
        return StatementMapper.INSTANCE.toDto(saved);
    }
    
    public Optional<StatementDto> findById(UUID id) {
        return repository.findById(id)
                .map(StatementMapper.INSTANCE::toDto);
    }
    
    public List<StatementDto> findAll() {
        return repository.findAll().stream()
                .map(StatementMapper.INSTANCE::toDto)
                .toList();
    }
    
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
    
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
    
    public boolean existsByTextAndAnswer(String text, String answer) {
        if (answer == null || answer.isEmpty()) {
            return repository.existsByTextAndAnswerIsNull(text);
        }
        return repository.existsByTextAndAnswer(text, answer);
    }
    
    public List<StatementDto> findByTextContaining(String textFragment) {
        return repository.findByTextContaining(textFragment).stream()
                .map(StatementMapper.INSTANCE::toDto)
                .toList();
    }
    
    public void flush() {
        repository.flush();
    }
    
    public void addFigureToStatement(UUID statementId, com.fcj.problem_suggest.model.FigureMetadata metadata) {
        Statement statement = repository.findById(statementId)
            .orElseThrow(() -> new RuntimeException("Statement not found: " + statementId));
            
        com.fcj.problem_suggest.model.Figure figure = new com.fcj.problem_suggest.model.Figure();
        figure.setMetadata(metadata);
        
        statement.setFigure(figure);
        repository.save(statement);
    }
}