package com.fcj.problem_suggest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatementDto {
    private UUID id;
    private String text;
    private String answer;
    private FigureDto figure;
    private Set<TagDto> tags;
}