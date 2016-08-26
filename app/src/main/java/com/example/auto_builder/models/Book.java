package com.example.auto_builder.models;

import com.example.AutoBuilder;

@AutoBuilder
public class Book {
    private String title;
    private String author;

    Book(BookBuilder builder) {
        title = builder.title;
        author = builder.author;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }
}
