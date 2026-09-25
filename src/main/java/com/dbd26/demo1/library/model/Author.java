package com.dbd26.demo1.library.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "author")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_author")
    private Long id;

    @Column(length = 100, nullable = false)
    private String fullname;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    /*
     Lado inverso de la relacion 1:N (mappedBy): la FK "id_author" vive en la tabla book, aca no hay columna.
     FetchType.LAZY: los libros se consultan recien cuando se accede a la coleccion
     (o de antemano con "join fetch", ver AuthorRepository.findByIdWithBooks).
     Acceder a la coleccion fuera de una transaccion lanza LazyInitializationException.
     */
    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private List<Book> books = new ArrayList<>();

    /*
     Bloqueo optimista: Hibernate incrementa "version" en cada UPDATE y falla (OptimisticLockException)
     si alguien modifico la fila entre que se leyo y se escribio.
     */
    @Version
    @Column(name = "version")
    private int version;

    public Author() {
    }

    public Author(String fullname, LocalDate dateOfBirth) {
        this.fullname = fullname;
        this.dateOfBirth = dateOfBirth;
    }

    public Author(Long id, String fullname, LocalDate dateOfBirth) {
        this.id = id;
        this.fullname = fullname;
        this.dateOfBirth = dateOfBirth;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
