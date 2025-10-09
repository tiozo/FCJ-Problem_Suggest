package com.fcj.problem_suggest.service.data;

import com.fcj.problem_suggest.dto.FigureDto;
import com.fcj.problem_suggest.mapper.FigureMapper;
import com.fcj.problem_suggest.model.Figure;
import com.fcj.problem_suggest.model.Statement;
import com.fcj.problem_suggest.repository.FigureRepository;
import com.fcj.problem_suggest.repository.StatementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FigureService {
    
    private final FigureRepository repository;
    private final StatementRepository statementRepository;
    
    public FigureDto save(FigureDto dto) {
        Statement statement = statementRepository.findById(dto.getStatementId())
            .orElseThrow(() -> new RuntimeException("Statement not found: " + dto.getStatementId()));
            
        Figure entity = FigureMapper.INSTANCE.toEntity(dto);
        entity.setStatement(statement);
        
        Figure saved = repository.save(entity);
        return FigureMapper.INSTANCE.toDto(saved);
    }
    
    public Optional<FigureDto> findById(UUID id) {
        return repository.findById(id)
                .map(FigureMapper.INSTANCE::toDto);
    }
    
    public List<FigureDto> findAll() {
        return repository.findAll().stream()
                .map(FigureMapper.INSTANCE::toDto)
                .toList();
    }
    
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
    
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
    
    public boolean existsByStatementId(UUID statementId) {
        return statementRepository.findById(statementId)
            .map(statement -> statement.getFigure() != null)
            .orElse(false);
    }
}