package com.dbd26.demo1.library.model;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "book")
@TypeAlias("Digital")
public class DigitalBook extends Book {

    private String format;

    private float size;

    public DigitalBook() {
    }

    public DigitalBook(String isbn, String title, int year, String comments, String format, float size, Author author) {
        super(isbn, title, year, comments, author);
        this.format = format;
        this.size = size;
    }

    public DigitalBook(String id, String isbn, String title, int year, String comments, String format, float size, Author author) {
        super(id, isbn, title, year, comments, author);
        this.format = format;
        this.size = size;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }
}
