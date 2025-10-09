package com.fcj.problem_suggest.dto;

import com.fcj.problem_suggest.model.FigureMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FigureDto {
    private UUID id;
    private UUID statementId;
    private FigureMetadata metadata;
}