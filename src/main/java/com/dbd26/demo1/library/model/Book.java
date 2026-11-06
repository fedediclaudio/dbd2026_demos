package com.dbd26.demo1.library.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

// Herencia: todas las subclases van a la coleccion "book" y el campo "_class" (ver @TypeAlias) hace de discriminador.
// Cada documento tiene solo los campos de su subclase.
@Document(collection = "book")
public abstract class Book {

    @Id
    private String id;

    // Indice unico: la unica restriccion que MongoDB impone ademas de la clave. Se crea al arrancar (auto-index-creation).
    @Indexed(unique = true)
    private String isbn;

    private String title;

    private int year;

    private String comments;

    // Lado propietario: guarda solo el _id del autor (como la FK id_author), sin validacion de claves foraneas.
    // La alternativa tipica en MongoDB es embeber una copia del autor en el libro.
    @DocumentReference
    @Indexed
    private Author author;

    @Transient // Indica que dicho atributo no se guarda en el documento.
    private Editorial editorial;

    @Transient
    private List<Genre> genre;

    public Book() {
    }

    public Book(String isbn, String title, int year, String comments, Author author) {
        this.isbn = isbn;
        this.title = title;
        this.year = year;
        this.comments = comments;
        this.author = author;
    }

    public Book(String id, String isbn, String title, int year, String comments, Author author) {
        this.id = id;
        this.isbn = isbn;
        this.title = title;
        this.year = year;
        this.comments = comments;
        this.author = author;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }
}
