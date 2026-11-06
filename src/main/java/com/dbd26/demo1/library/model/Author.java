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

/*
 @Document: la clase se guarda como documentos BSON en la coleccion "author" (equivale a @Entity + @Table).
 No hay esquema: cada documento tiene los campos que tenga el objeto al guardarlo.
 */
@Document(collection = "author")
public class Author {

    /*
     Clave del documento (campo "_id"). Si se deja en null, MongoDB genera un ObjectId (12 bytes: fecha, maquina,
     contador), que Spring Data representa como String hexadecimal de 24 caracteres.
     */
    @Id
    private String id;

    @Field("fullname")
    private String fullname;

    @Field("date_of_birth")
    private LocalDate dateOfBirth;

    /*
     Lado "inverso" de la relacion, equivalente al mappedBy de JPA: esta lista NO se guarda en el documento
     (@ReadOnlyProperty). Al leer un autor, Spring Data la resuelve con la consulta del "lookup":
     busca en la coleccion book los documentos cuyo campo "author" sea el id de este autor.
     lazy = true: esa consulta se ejecuta recien cuando se accede a la lista (misma idea que FetchType.LAZY).
     */
    @ReadOnlyProperty
    @DocumentReference(lookup = "{ 'author': ?#{#self._id} }", lazy = true)
    private List<Book> books = new ArrayList<>();

    /*
     Bloqueo optimista: Spring Data incrementa "version" en cada save() y falla (OptimisticLockingFailureException)
     si el documento fue modificado entre la lectura y la escritura. Debe ser un tipo envoltorio (Integer, no int):
     Spring Data usa "version == null" para saber si el objeto es nuevo (insert) o existente (update).
     */
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
