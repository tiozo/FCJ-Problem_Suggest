package com.fcj.problem_suggest.service;

import org.springframework.stereotype.Service;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ToQdrant implements MarkdownProcessor {
    
    private final EmbeddingService embeddingService;
    private final QdrantService qdrantService;
    
    public ToQdrant(EmbeddingService embeddingService, QdrantService qdrantService) {
        this.embeddingService = embeddingService;
        this.qdrantService = qdrantService;
    }
    
    public void process(String markdown) {
        try {
            Pattern questionPattern = Pattern.compile("### (Question \\d+|Câu \\d+)\\s*\\n(.*?)(?=\\n### |$)", Pattern.DOTALL);
            Matcher matcher = questionPattern.matcher(markdown);
            
            while (matcher.find()) {
                String questionContent = matcher.group(2).trim();
                
                // Extract problem statement (before first (*))
                String problemStatement = extractProblemStatement(questionContent);
                
                // Extract answer choices
                String problemAnswer = extractAnswerChoices(questionContent);
                
                if (!problemStatement.isEmpty()) {
                    float[] vector = embeddingService.embedStatement(problemStatement);
                    qdrantService.addPoint(vector, problemStatement, problemAnswer);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process markdown", e);
        }
    }
    
    private String extractProblemStatement(String content) {
        // Find first occurrence of figure marker '[' or answer choices
        int figureIndex = content.indexOf("[");
        int answerIndexUpper = content.indexOf("\nA.");
        int answerIndexLower = content.indexOf("\na)");
        
        int endIndex = -1;
        
        // Find the earliest occurrence among figure, uppercase answers, lowercase answers
        if (figureIndex != -1) endIndex = figureIndex;
        if (answerIndexUpper != -1 && (endIndex == -1 || answerIndexUpper < endIndex)) endIndex = answerIndexUpper;
        if (answerIndexLower != -1 && (endIndex == -1 || answerIndexLower < endIndex)) endIndex = answerIndexLower;
        
        if (endIndex != -1) {
            return content.substring(0, endIndex).trim();
        }
        
        return content.trim(); // return entire content if no markers found
    }
    
    private String extractAnswerChoices(String content) {
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
            String answerSection = content.substring(answerIndex + 1); // +1 to skip the \n
            // Stop at section headers (##)
            int sectionIndex = answerSection.indexOf("##");
            if (sectionIndex != -1) {
                answerSection = answerSection.substring(0, sectionIndex);
            }
            
            return answerSection.trim();
        }
        return ""; // No answer choices found
    }
}
