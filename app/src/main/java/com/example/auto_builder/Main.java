package com.example.auto_builder;

import com.example.auto_builder.models.Book;
import com.example.auto_builder.models.BookBuilder;

public class Main {
    public Main() {
        Book book = new BookBuilder()
                .title("Book Title")
                .author("Book Author")
                .build();
    }
}
