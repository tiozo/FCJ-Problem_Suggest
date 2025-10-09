package com.fcj.problem_suggest.service;

import com.fcj.problem_suggest.dto.FigureDto;
import com.fcj.problem_suggest.dto.StatementDto;
import com.fcj.problem_suggest.model.FigureMetadata;
import com.fcj.problem_suggest.service.data.FigureService;
import com.fcj.problem_suggest.service.data.StatementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ToDB implements MarkdownProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(ToDB.class);
    
    private final EmbeddingService embeddingService;
    private final QdrantService qdrantService;
    private final StatementService statementService;
    private final FigureService figureService;
    
    @Value("${aws.s3.bucket}")
    private String bucketName;
    
    public ToDB(EmbeddingService embeddingService, QdrantService qdrantService, 
               StatementService statementService, FigureService figureService) {
        this.embeddingService = embeddingService;
        this.qdrantService = qdrantService;
        this.statementService = statementService;
        this.figureService = figureService;
    }
    
    @Override
    public void process(String markdown) {
        process(markdown, null);
    }

    @Override
    public void process(String markdown, String pdfName) {
        int questionCount = 0;
        try {
            Pattern questionPattern = Pattern.compile("### (Question \\d+|Câu \\d+)\\s*\\n(.*?)(?=\\n### |$)", Pattern.DOTALL);
            Matcher matcher = questionPattern.matcher(markdown);
            
            while (matcher.find()) {
                questionCount++;
                processQuestion(matcher.group(2).trim(), pdfName, questionCount);
            }

        } catch (Exception e) {
            logger.error("Failed to process markdown after {} questions: {}", questionCount, e.getMessage());
            throw new RuntimeException("Failed to process markdown after question " + questionCount, e);
        }
    }
    
    private void processQuestion(String questionContent, String pdfName, int questionCount) {
        try {
            String problemStatement = extractProblemStatement(questionContent);
            String problemAnswer = extractAnswerChoices(questionContent);
            
            if (isValidProblemStatement(problemStatement)) {
                problemStatement = truncateText(problemStatement, 2000);
                String answerToCheck = prepareAnswer(problemAnswer);
                
                if (!statementService.existsByTextAndAnswer(problemStatement, answerToCheck)) {
                    StatementDto savedStatement = saveStatement(problemStatement, answerToCheck);
                    processFigureIfExists(questionContent, pdfName, questionCount, savedStatement);
                    addToQdrant(problemStatement, savedStatement.getId(), questionCount);
                }
            }
        } catch (Exception questionException) {
            logger.error("Failed to process question {}: {}", questionCount, questionException.getMessage());
        }
    }
    
    private String truncateText(String text, int maxLength) {
        return text.length() > maxLength ? text.substring(0, maxLength - 3) + "..." : text;
    }
    
    private String prepareAnswer(String problemAnswer) {
        String answerToCheck = problemAnswer.isEmpty() ? null : problemAnswer;
        return answerToCheck != null && answerToCheck.length() > 1000 
            ? answerToCheck.substring(0, 997) + "..." 
            : answerToCheck;
    }
    
    private StatementDto saveStatement(String problemStatement, String answerToCheck) {
        StatementDto statementDto = new StatementDto();
        statementDto.setText(problemStatement);
        statementDto.setAnswer(answerToCheck);
        logger.debug("Creating statement DTO with ID: {}", statementDto.getId());
        
        StatementDto savedStatement = statementService.save(statementDto);
        logger.debug("Saved statement with ID: {}", savedStatement.getId());
        
        statementService.flush();
        logger.debug("Statement flushed to database");
        
        return savedStatement;
    }
    
    private void processFigureIfExists(String questionContent, String pdfName, int questionCount, StatementDto savedStatement) {
        if (pdfName != null && questionContent.contains("figure_")) {
            int figureOrder = extractFigureNumber(questionContent);
            if (figureOrder > 0) {
                logger.info("Processing figure {} for question {}, statementId: {}", figureOrder, questionCount, savedStatement.getId());
                saveFigureForStatement(savedStatement.getId(), pdfName, figureOrder);
            }
        }
    }
    
    private void addToQdrant(String problemStatement, java.util.UUID statementId, int questionCount) {
        try {
            float[] vector = embeddingService.embedStatement(problemStatement);
            qdrantService.addPoint(vector, problemStatement, statementId);
        } catch (Exception vectorException) {
            logger.error("Failed to add to Qdrant for question {}: {}", questionCount, vectorException.getMessage());
        }
    }
    
    private boolean isValidProblemStatement(String statement) {
        return statement != null && 
               !statement.trim().isEmpty() && 
               statement.trim().length() > 5;
    }
    
    private int extractFigureNumber(String content) {
        Pattern figurePattern = Pattern.compile("figure_(\\d+)");
        Matcher matcher = figurePattern.matcher(content);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return -1;
    }

    private void saveFigureForStatement(java.util.UUID statementId, String pdfName, int figureOrder) {
        logger.debug("Attempting to save figure {} for statement {}", figureOrder, statementId);
        try {
            if (figureService.existsByStatementId(statementId)) {
                logger.debug("Figure already exists for statement {}, skipping", statementId);
                return;
            }
            
            String s3Url = "https://" + bucketName + ".s3.amazonaws.com/figures/" + pdfName + "/" + figureOrder + ".png";
            FigureMetadata metadata = new FigureMetadata(s3Url, Instant.now());
            
            logger.debug("Saving figure {} with URL: {}", figureOrder, s3Url);
            statementService.addFigureToStatement(statementId, metadata);
            logger.info("Successfully saved figure {}", figureOrder);
        } catch (Exception e) {
            logger.error("Failed to save figure {} to database: {}", figureOrder, e.getMessage());
        }
    }
    
    private String extractProblemStatement(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        
        int figureIndex = content.indexOf("[");
        int answerIndexUpper = content.indexOf("\nA.");
        int answerIndexLower = content.indexOf("\na)");
        
        int endIndex = -1;
        
        if (figureIndex != -1) endIndex = figureIndex;
        if (answerIndexUpper != -1 && (endIndex == -1 || answerIndexUpper < endIndex)) endIndex = answerIndexUpper;
        if (answerIndexLower != -1 && (endIndex == -1 || answerIndexLower < endIndex)) endIndex = answerIndexLower;
        
        String result;
        if (endIndex != -1) {
            result = content.substring(0, endIndex).trim();
        } else {
            result = content.trim();
        }
        
        return result.isEmpty() ? null : result;
    }
    
    private String extractAnswerChoices(String content) {
        if (content == null) return "";
        
        int answerIndexUpper = content.indexOf("\nA.");
        int answerIndexLower = content.indexOf("\na)");
        
        int answerIndex = -1;
        if (answerIndexUpper != -1 && answerIndexLower != -1) {
            answerIndex = Math.min(answerIndexUpper, answerIndexLower);
        } else if (answerIndexUpper != -1) {
            answerIndex = answerIndexUpper;
        } else if (answerIndexLower != -1) {
            answerIndex = answerIndexLower;
        }
        
        if (answerIndex != -1) {
            String answerSection = content.substring(answerIndex + 1);
            int sectionIndex = answerSection.indexOf("##");
            if (sectionIndex != -1) {
                answerSection = answerSection.substring(0, sectionIndex);
            }
            
            return answerSection.trim();
        }
        return "";
    }
}