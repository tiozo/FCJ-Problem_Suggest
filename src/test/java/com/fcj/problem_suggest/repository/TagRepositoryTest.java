package com.fcj.problem_suggest.repository;

import com.fcj.problem_suggest.model.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TagRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TagRepository repository;

    @Test
    void save_shouldPersistTag() {
        Tag tag = new Tag();
        tag.setName("geometry");

        Tag saved = repository.save(tag);

        assertNotNull(saved.getId());
        assertEquals("geometry", saved.getName());
    }

    @Test
    void findAll_shouldReturnAllTags() {
        Tag tag1 = new Tag();
        tag1.setName("algebra");
        Tag tag2 = new Tag();
        tag2.setName("geometry");
        
        entityManager.persistAndFlush(tag1);
        entityManager.persistAndFlush(tag2);

        List<Tag> tags = repository.findAll();

        assertEquals(2, tags.size());
    }

    @Test
    void deleteById_shouldRemoveTag() {
        Tag tag = new Tag();
        tag.setName("calculus");
        Tag persisted = entityManager.persistAndFlush(tag);

        repository.deleteById(persisted.getId());

        Optional<Tag> found = repository.findById(persisted.getId());
        assertFalse(found.isPresent());
    }
}