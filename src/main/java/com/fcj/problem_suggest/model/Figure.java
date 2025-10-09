package com.fcj.problem_suggest.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Figure")
@Table(name = "figure")
public class Figure {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "JSON")
    private FigureMetadata metadata;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "statement_id", unique = true)
    private Statement statement;
}
