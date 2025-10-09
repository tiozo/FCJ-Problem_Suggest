package com.fcj.problem_suggest.service.data;

import com.fcj.problem_suggest.dto.TagDto;
import com.fcj.problem_suggest.mapper.TagMapper;
import com.fcj.problem_suggest.model.Tag;
import com.fcj.problem_suggest.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TagService {
    
    private final TagRepository repository;
    
    public TagDto save(TagDto dto) {
        Tag entity = TagMapper.INSTANCE.toEntity(dto);
        Tag saved = repository.save(entity);
        return TagMapper.INSTANCE.toDto(saved);
    }
    
    public Optional<TagDto> findById(UUID id) {
        return repository.findById(id)
                .map(TagMapper.INSTANCE::toDto);
    }
    
    public List<TagDto> findAll() {
        return repository.findAll().stream()
                .map(TagMapper.INSTANCE::toDto)
                .toList();
    }
    
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
    
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
}