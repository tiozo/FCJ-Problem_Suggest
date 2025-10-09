package com.fcj.problem_suggest.repository;

import com.fcj.problem_suggest.model.Figure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FigureRepository extends JpaRepository<Figure, UUID> {
}