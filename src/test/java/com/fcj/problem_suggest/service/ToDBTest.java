package com.fcj.problem_suggest.service;

import com.fcj.problem_suggest.dto.StatementDto;
import com.fcj.problem_suggest.model.FigureMetadata;
import com.fcj.problem_suggest.service.data.FigureService;
import com.fcj.problem_suggest.service.data.StatementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ToDBTest {

    @Mock
    private EmbeddingService embeddingService;
    
    @Mock
    private QdrantService qdrantService;
    
    @Mock
    private StatementService statementService;
    
    @Mock
    private FigureService figureService;
    
    @InjectMocks
    private ToDB toDB;

    @Test
    void testProcessMarkdownWithFigure() throws Exception {
        // Setup
        ReflectionTestUtils.setField(toDB, "bucketName", "test-bucket");
        
        String markdown = """
            ### Question 1
            This is a problem statement with figure_5
            
            A. Option A
            B. Option B
            C. Option C
            D. Option D
            """;
        
        UUID statementId = UUID.randomUUID();
        StatementDto savedStatement = new StatementDto();
        savedStatement.setId(statementId);
        savedStatement.setText("This is a problem statement with figure_5");
        
        // Mock behavior
        when(statementService.existsByTextAndAnswer(anyString(), anyString())).thenReturn(false);
        when(statementService.save(any(StatementDto.class))).thenReturn(savedStatement);
        when(figureService.existsByStatementId(statementId)).thenReturn(false);
        when(embeddingService.embedStatement(anyString())).thenReturn(new float[]{1.0f, 2.0f});
        
        // Execute
        toDB.process(markdown, "sample");
        
        // Verify
        verify(statementService).save(any(StatementDto.class));
        verify(statementService).flush();
        verify(statementService).addFigureToStatement(eq(statementId), any(FigureMetadata.class));
        verify(embeddingService).embedStatement(anyString());
        verify(qdrantService).addPoint(any(float[].class), anyString(), eq(statementId));
    }
    
    @Test
    void testProcessMarkdownWithoutFigure() throws Exception {
        // Setup
        String markdown = """
            ### Question 1
            This is a problem statement without figure
            
            A. Option A
            B. Option B
            """;
        
        UUID statementId = UUID.randomUUID();
        StatementDto savedStatement = new StatementDto();
        savedStatement.setId(statementId);
        
        // Mock behavior
        when(statementService.existsByTextAndAnswer(anyString(), anyString())).thenReturn(false);
        when(statementService.save(any(StatementDto.class))).thenReturn(savedStatement);
        when(embeddingService.embedStatement(anyString())).thenReturn(new float[]{1.0f, 2.0f});
        
        // Execute
        toDB.process(markdown, "sample");
        
        // Verify
        verify(statementService).save(any(StatementDto.class));
        verify(statementService).flush();
        verify(statementService, never()).addFigureToStatement(any(), any());
        verify(embeddingService).embedStatement(anyString());
    }
}