package com.fcj.problem_suggest.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity(name = "Statement")
@Table(name = "statement")
public class Statement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "text", nullable = false, columnDefinition = "NVARCHAR(2000)")
    private String text;

    @Column(name = "answer", columnDefinition = "NVARCHAR(1000)")
    private String answer;

    @OneToOne(mappedBy = "statement", cascade = CascadeType.ALL, orphanRemoval = true)
    private Figure figure;

    @ManyToMany(
        cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinTable(
            name = "statement_tag",
            joinColumns = @JoinColumn(name = "statement_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    public void setFigure(Figure figure) {
        if (figure != null) {
            figure.setStatement(this);
        }
        this.figure = figure;
    }

    public void addTag(@NotNull Tag tag) {
        tags.add(tag);
        tag.getStatements().add(this);
    }

    public void removeTag(@NotNull Tag tag) {
        tags.remove(tag);
        tag.getStatements().remove(this);
    }
}