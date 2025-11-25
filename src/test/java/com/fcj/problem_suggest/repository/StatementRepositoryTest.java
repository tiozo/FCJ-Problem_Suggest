package com.fcj.problem_suggest.repository;

import com.fcj.problem_suggest.model.Statement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class StatementRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StatementRepository repository;

    @Test
    void save_shouldPersistStatement() {
        Statement statement = new Statement();
        statement.setText("Test problem");
        statement.setAnswer("Test answer");

        Statement saved = repository.save(statement);

        assertNotNull(saved.getId());
        assertEquals("Test problem", saved.getText());
        assertEquals("Test answer", saved.getAnswer());
    }

    @Test
    void findById_shouldReturnStatement() {
        Statement statement = new Statement();
        statement.setText("Test problem");
        statement.setAnswer("Test answer");
        Statement persisted = entityManager.persistAndFlush(statement);

        Optional<Statement> found = repository.findById(persisted.getId());

        assertTrue(found.isPresent());
        assertEquals("Test problem", found.get().getText());
    }

    @Test
    void deleteById_shouldRemoveStatement() {
        Statement statement = new Statement();
        statement.setText("Test problem");
        statement.setAnswer("Test answer");
        Statement persisted = entityManager.persistAndFlush(statement);

        repository.deleteById(persisted.getId());

        Optional<Statement> found = repository.findById(persisted.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void existsById_shouldReturnTrue() {
        Statement statement = new Statement();
        statement.setText("Test problem");
        statement.setAnswer("Test answer");
        Statement persisted = entityManager.persistAndFlush(statement);

        assertTrue(repository.existsById(persisted.getId()));
        assertFalse(repository.existsById(UUID.randomUUID()));
    }
}