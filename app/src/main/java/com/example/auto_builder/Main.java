package com.example.auto_builder;

import com.example.auto_builder.models.Book;

public class Main {
    public static void main(String[] args) {
        Book book = new Book.Builder()
                .title("Book Title")
                .author("Book Author")
                .build();

        System.out.println(book.getAuthor());
        System.out.println(book.getTitle());
    }
}
