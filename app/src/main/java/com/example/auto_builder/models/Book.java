package com.example.auto_builder.models;

//@AutoBuilder
public class Book {
    private String title;
    private String author;

    private Book(Builder builder) {
        title = builder.title;
        author = builder.author;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public static class Builder {
        private String title;
        private String author;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Book build() {
            return new Book(this);
        }
    }
}
