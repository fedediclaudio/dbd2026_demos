package com.dbd26.demo1.library.model;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

// @TypeAlias: valor que se guarda en el campo "_class" del documento (equivale a @DiscriminatorValue).
// Sin esta anotacion Spring Data guardaria el nombre completo de la clase.
@Document(collection = "book")
@TypeAlias("Physical")
public class PhysicalBook extends Book {

    private float weight;

    private String location;

    public PhysicalBook() {
    }

    public PhysicalBook(String isbn, String title, int year, String comments, float weight, String location, Author author) {
        super(isbn, title, year, comments, author);
        this.weight = weight;
        this.location = location;
    }

    public PhysicalBook(String id, String isbn, String title, int year, String comments, float weight, String location, Author author) {
        super(id, isbn, title, year, comments, author);
        this.weight = weight;
        this.location = location;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
