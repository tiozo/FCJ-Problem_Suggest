package com.fcj.problem_suggest.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UuidConsistencyTest {

    @Test
    void statementAndFigure_shouldHaveConsistentUuids() {
        Statement statement = new Statement();
        UUID statementId = UUID.randomUUID();
        statement.setId(statementId);
        
        Figure figure = new Figure();
        figure.setStatement(statement);
        figure.setStatementId(statementId);
        
        assertEquals(statement.getId(), figure.getStatementId());
    }

    @Test
    void figureWithInconsistentUuid_shouldDetectMismatch() {
        Statement statement = new Statement();
        statement.setId(UUID.randomUUID());
        
        Figure figure = new Figure();
        figure.setStatement(statement);
        figure.setStatementId(UUID.randomUUID());
        
        assertNotEquals(statement.getId(), figure.getStatementId());
    }

    @Test
    void figureWithNullStatement_shouldHandleGracefully() {
        Figure figure = new Figure();
        UUID figureId = UUID.randomUUID();
        figure.setStatementId(figureId);
        
        assertNull(figure.getStatement());
        assertNotNull(figure.getStatementId());
    }

    @Test
    void statementWithNullFigure_shouldHandleGracefully() {
        Statement statement = new Statement();
        UUID statementId = UUID.randomUUID();
        statement.setId(statementId);
        
        assertNull(statement.getFigure());
        assertNotNull(statement.getId());
    }

    @Test
    void multipleStatementsWithSameUuid_shouldDetectDuplicate() {
        UUID sharedId = UUID.randomUUID();
        
        Statement s1 = new Statement();
        s1.setId(sharedId);
        
        Statement s2 = new Statement();
        s2.setId(sharedId);
        
        assertEquals(s1.getId(), s2.getId());
        assertTrue(s1.getId().equals(s2.getId()));
    }
}