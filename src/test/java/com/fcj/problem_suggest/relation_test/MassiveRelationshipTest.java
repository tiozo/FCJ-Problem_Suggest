package com.fcj.problem_suggest.relation_test;

import com.fcj.problem_suggest.model.Statement;
import com.fcj.problem_suggest.model.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MassiveRelationshipTest {

    @Test
    void massiveRelationship_1000Problems_1000Tags_shouldHandleComplexity() {
        List<Statement> statements = new ArrayList<>();
        List<Tag> tags = new ArrayList<>();
        
        // Create 1000 statements
        for (int i = 0; i < 1000; i++) {
            Statement s = new Statement();
            s.setText("Problem " + i);
            s.setAnswer("Answer " + i);
            statements.add(s);
        }
        
        // Create 1000 tags
        for (int i = 0; i < 1000; i++) {
            Tag t = new Tag();
            t.setName("tag_" + i);
            tags.add(t);
        }
        
        // Each of 1000 problems gets all 1000 tags
        for (Statement statement : statements) {
            for (Tag tag : tags) {
                statement.getTags().add(tag);
                tag.getStatements().add(statement);
            }
        }
        
        // Each of 1000 tags should have exactly 1000 problems (all problems)
        for (Tag tag : tags) {
            assertEquals(1000, tag.getStatements().size());
        }
        
        // Each of 1000 problems should have exactly 1000 tags (all tags)
        for (Statement statement : statements) {
            assertEquals(1000, statement.getTags().size());
        }
        
        assertDoesNotThrow(() -> {
            Set<Statement> statementSet = new HashSet<>(statements);
            Set<Tag> tagSet = new HashSet<>(tags);
            
            assertEquals(1000, statementSet.size());
            assertEquals(1000, tagSet.size());
            
            // Test toString doesn't cause stack overflow
            statements.get(0).toString();
            tags.get(0).toString();
        });
    }

    @Test
    void partialRelationship_1000Tags_100ProblemsEach_shouldHandleAsymmetry() {
        List<Statement> allStatements = new ArrayList<>();
        List<Tag> tags = new ArrayList<>();
        
        // Create 1000 tags
        for (int i = 0; i < 1000; i++) {
            Tag t = new Tag();
            t.setName("tag_" + i);
            tags.add(t);
        }
        
        // For each tag, create 100 unique problems
        for (int tagIndex = 0; tagIndex < 1000; tagIndex++) {
            Tag currentTag = tags.get(tagIndex);
            
            for (int problemIndex = 0; problemIndex < 100; problemIndex++) {
                Statement s = new Statement();
                s.setText("Problem_" + tagIndex + "_" + problemIndex);
                s.setAnswer("Answer_" + tagIndex + "_" + problemIndex);
                
                s.getTags().add(currentTag);
                currentTag.getStatements().add(s);
                
                allStatements.add(s);
            }
        }
        
        // Verify: 1000 tags, each with exactly 100 problems
        for (Tag tag : tags) {
            assertEquals(100, tag.getStatements().size());
        }
        
        // Verify: 100,000 total problems, each with exactly 1 tag
        assertEquals(100000, allStatements.size());
        for (Statement statement : allStatements) {
            assertEquals(1, statement.getTags().size());
        }
        
        assertDoesNotThrow(() -> {
            Set<Statement> statementSet = new HashSet<>(allStatements);
            Set<Tag> tagSet = new HashSet<>(tags);
            
            assertEquals(100000, statementSet.size());
            assertEquals(1000, tagSet.size());
        });
    }
}