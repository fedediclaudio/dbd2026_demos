package com.dbd26.demo1.library.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

/*
 Herencia en MongoDB: no hay tablas, asi que no hay SINGLE_TABLE ni JOINED. Todas las subclases se guardan en la
 misma coleccion "book" y Spring Data agrega a cada documento el campo "_class" con el alias de la subclase
 ("Physical" o "Digital", ver @TypeAlias en PhysicalBook y DigitalBook). Es el equivalente de la columna
 discriminadora: al leer, Spring Data mira "_class" para saber que subclase instanciar.
 Cada documento tiene solo los campos de su subclase: un libro fisico no tiene "format" ni "size" en null,
 directamente no tiene esos campos.
 */
@Document(collection = "book")
public abstract class Book {

    @Id
    private String id;

    // Indice unico sobre isbn: es la unica "restriccion" que MongoDB puede imponer (ademas de la clave).
    // Se crea al arrancar gracias a spring.data.mongodb.auto-index-creation=true.
    @Indexed(unique = true)
    private String isbn;

    private String title;

    private int year;

    private String comments;

    /*
     Lado propietario de la relacion: el documento guarda en el campo "author" solo el _id del autor
     (equivale a la FK id_author). Al leer un libro, Spring Data hace una segunda consulta para traer el autor.
     Alternativa tipica en MongoDB: embeber una copia del autor dentro del libro (desnormalizar), que evita la
     segunda consulta pero duplica datos.
     MongoDB NO valida la referencia: nada impide borrar un autor que tiene libros (no hay claves foraneas).
     */
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
