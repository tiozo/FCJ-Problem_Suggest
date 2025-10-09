package com.fcj.problem_suggest.relation_test;

import com.fcj.problem_suggest.model.Figure;
import com.fcj.problem_suggest.model.Statement;
import com.fcj.problem_suggest.model.Tag;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class EntityRelationshipTest {

    @Test
    void statementAndFigure_circularReference_shouldNotCauseStackOverflow() {
        Statement statement = new Statement();
        Figure figure = new Figure();
        
        figure.setStatement(statement);
        statement.setFigure(figure);
        
        assertDoesNotThrow(() -> {
            Set<Statement> statements = new HashSet<>();
            statements.add(statement);
            Set<Figure> figures = new HashSet<>();
            figures.add(figure);
            
            statement.toString();
            figure.toString();
        });
    }

    @Test
    void manyToMany_circularReference_shouldNotCauseStackOverflow() {
        Statement s1 = new Statement();
        Statement s2 = new Statement();
        Tag t1 = new Tag();
        Tag t2 = new Tag();
        
        s1.getTags().add(t1);
        s1.getTags().add(t2);
        s2.getTags().add(t1);
        
        t1.getStatements().add(s1);
        t1.getStatements().add(s2);
        t2.getStatements().add(s1);
        
        assertDoesNotThrow(() -> {
            Set<Statement> statements = new HashSet<>(Arrays.asList(s1, s2));
            Set<Tag> tags = new HashSet<>(Arrays.asList(t1, t2));
            
            s1.toString();
            t1.toString();
        });
    }

    @Test
    void largeDataSet_shouldHandlePerformance() {
        List<Statement> statements = new ArrayList<>();
        List<Tag> tags = new ArrayList<>();
        
        for (int i = 0; i < 1000; i++) {
            Statement s = new Statement();
            s.setText("Problem " + i);
            s.setAnswer("Answer " + i);
            statements.add(s);
            
            if (i < 100) {
                Tag t = new Tag();
                t.setName("tag" + i);
                tags.add(t);
            }
        }
        
        for (Statement s : statements) {
            Tag randomTag = tags.get(s.hashCode() % tags.size());
            s.getTags().add(randomTag);
            randomTag.getStatements().add(s);
        }
        
        assertDoesNotThrow(() -> {
            Set<Statement> statementSet = new HashSet<>(statements);
            Set<Tag> tagSet = new HashSet<>(tags);
            assertEquals(1000, statementSet.size());
            assertEquals(100, tagSet.size());
        });
    }

    @Test
    void nullValues_shouldHandleGracefully() {
        Statement statement = new Statement();
        
        assertDoesNotThrow(() -> {
            statement.setFigure(null);
            statement.getTags().add(null);
            
            Set<Statement> statements = new HashSet<>();
            statements.add(statement);
        });
    }

    @Test
    void extremelyLargeDataSet_shouldHandleMemoryPressure() {
        List<Statement> statements = new ArrayList<>();
        List<Tag> tags = new ArrayList<>();
        
        for (int i = 0; i < 10000; i++) {
            Statement s = new Statement();
            s.setText("Problem " + i + " with very long text content that simulates real world data size and complexity for memory testing purposes");
            s.setAnswer("Answer " + i + " with detailed explanation that could be quite lengthy in practice");
            statements.add(s);
            
            if (i < 500) {
                Tag t = new Tag();
                t.setName("category_" + i + "_with_longer_name");
                tags.add(t);
            }
        }
        
        for (Statement s : statements) {
            int tagCount = Math.abs(s.hashCode()) % 5 + 1;
            for (int j = 0; j < tagCount; j++) {
                Tag randomTag = tags.get((s.hashCode() + j) % tags.size());
                s.getTags().add(randomTag);
                randomTag.getStatements().add(s);
            }
        }
        
        assertDoesNotThrow(() -> {
            Set<Statement> statementSet = new HashSet<>(statements);
            Set<Tag> tagSet = new HashSet<>(tags);
            assertEquals(10000, statementSet.size());
            assertEquals(500, tagSet.size());
            
            statements.parallelStream().forEach(s -> {
                s.getTags().size();
                s.toString();
            });
        });
    }

    @Test
    void deeplyNestedRelationships_shouldNotCauseStackOverflow() {
        List<Statement> chain = new ArrayList<>();
        List<Tag> tagChain = new ArrayList<>();
        
        for (int i = 0; i < 1000; i++) {
            Statement s = new Statement();
            s.setText("Chain " + i);
            s.setAnswer("Answer " + i);
            chain.add(s);
            
            Tag t = new Tag();
            t.setName("tag_" + i);
            tagChain.add(t);
            
            s.getTags().add(t);
            t.getStatements().add(s);
            
            if (i > 0) {
                s.getTags().add(tagChain.get(i - 1));
                tagChain.get(i - 1).getStatements().add(s);
            }
        }
        
        assertDoesNotThrow(() -> {
            chain.get(999).toString();
            tagChain.get(999).toString();
            
            Set<Statement> allStatements = new HashSet<>(chain);
            Set<Tag> allTags = new HashSet<>(tagChain);
            
            assertEquals(1000, allStatements.size());
            assertEquals(1000, allTags.size());
        });
    }

    @Test
    void concurrentModification_shouldHandleGracefully() {
        Statement statement = new Statement();
        statement.setText("Concurrent test");
        statement.setAnswer("Test answer");
        
        List<Thread> threads = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            final int threadId = i;
            threads.add(new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    Tag tag = new Tag();
                    tag.setName("thread_" + threadId + "_tag_" + j);
                    
                    synchronized (statement) {
                        statement.getTags().add(tag);
                        tag.getStatements().add(statement);
                    }
                }
            }));
        }
        
        assertDoesNotThrow(() -> {
            threads.forEach(Thread::start);
            for (Thread t : threads) {
                t.join();
            }
            
            assertEquals(1000, statement.getTags().size());
        });
    }

    @Test
    void extremeEdgeCases_shouldHandleRobustly() {
        assertDoesNotThrow(() -> {
            Statement s1 = new Statement();
            Statement s2 = new Statement();
            Tag tag = new Tag();
            
            s1.getTags().add(tag);
            s2.getTags().add(tag);
            tag.getStatements().add(s1);
            tag.getStatements().add(s2);
            
            s1.getTags().remove(tag);
            tag.getStatements().remove(s1);
            
            s1.getTags().add(tag);
            tag.getStatements().add(s1);
            
            Set<Statement> statements = new HashSet<>();
            statements.add(s1);
            statements.add(s2);
            statements.add(null);
            statements.remove(null);
            
            Figure figure = new Figure();
            s1.setFigure(figure);
            s1.setFigure(null);
            s1.setFigure(figure);
            
            assertEquals(2, statements.size());
            assertNotNull(s1.getFigure());
        });
    }
}