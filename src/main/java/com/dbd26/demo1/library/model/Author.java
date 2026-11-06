package com.dbd26.demo1.library.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// @Document: equivale a @Entity + @Table. No hay esquema: cada documento tiene los campos del objeto guardado.
@Document(collection = "author")
public class Author {

    // Campo "_id". Si es null, MongoDB genera un ObjectId, que Spring Data representa como String de 24 hexadecimales.
    @Id
    private String id;

    @Field("fullname")
    private String fullname;

    @Field("date_of_birth")
    private LocalDate dateOfBirth;

    // Lado inverso (mappedBy): no se guarda, se resuelve con el lookup sobre book al acceder a la lista (lazy).
    @ReadOnlyProperty
    @DocumentReference(lookup = "{ 'author': ?#{#self._id} }", lazy = true)
    private List<Book> books = new ArrayList<>();

    // Bloqueo optimista. Integer y no int: version == null indica objeto nuevo (insert).
    @Version
    private Integer version;

    public Author() {
    }

    public Author(String fullname, LocalDate dateOfBirth) {
        this.fullname = fullname;
        this.dateOfBirth = dateOfBirth;
    }

    public Author(String id, String fullname, LocalDate dateOfBirth) {
        this.id = id;
        this.fullname = fullname;
        this.dateOfBirth = dateOfBirth;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
