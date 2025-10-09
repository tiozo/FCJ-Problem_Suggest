package com.fcj.problem_suggest.repository;

import com.fcj.problem_suggest.model.Statement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StatementRepository extends JpaRepository<Statement, UUID> {
    boolean existsByTextAndAnswer(String text, String answer);
    boolean existsByTextAndAnswerIsNull(String text);
    List<Statement> findByTextContaining(String textFragment);
}