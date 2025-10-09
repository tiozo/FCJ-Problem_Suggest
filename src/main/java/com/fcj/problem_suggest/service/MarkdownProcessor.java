package com.fcj.problem_suggest.service;

public interface MarkdownProcessor {
    void process(String markdown);
    default void process(String markdown, String pdfName) {
        process(markdown);
    }
}