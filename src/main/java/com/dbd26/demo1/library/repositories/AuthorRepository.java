package com.dbd26.demo1.library.repositories;

import com.dbd26.demo1.library.dto.AuthorBookCount;
import com.dbd26.demo1.library.model.Author;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/*
 MongoRepository: igual que JpaRepository, Spring Data genera la implementacion. Cambia el lenguaje de consulta:
 en lugar de JPQL, filtros JSON de MongoDB (@Query) y pipelines de agregacion (@Aggregation).
 */
@Repository
public interface AuthorRepository extends MongoRepository<Author, String> {

    // Consulta derivada: Spring Data la traduce a { date_of_birth: { $gt: <fecha> } }
    List<Author> findByDateOfBirthGreaterThan(LocalDate date);

    // Consulta derivada: se traduce a una expresion regular insensible a mayusculas { fullname: /texto/i }
    List<Author> findByFullnameContainingIgnoreCase(String text);

    // Agregacion: no hay "join" en una consulta comun, pero el pipeline tiene $lookup, que trae los documentos
    // de otra coleccion. Aca se cuentan los libros de cada autor, se filtran los que tienen alguno y se ordenan.
    // $unset quita los campos auxiliares para que el documento resultante se mapee a Author sin problemas.
    @Aggregation(pipeline = {
            "{ $lookup: { from: 'book', localField: '_id', foreignField: 'author', as: 'bookDocs' } }",
            "{ $addFields: { bookCount: { $size: '$bookDocs' } } }",
            "{ $match: { bookCount: { $gt: 0 } } }",
            "{ $sort: { bookCount: -1 } }",
            "{ $unset: ['bookDocs', 'bookCount'] }"
    })
    List<Author> findByMoreBooks();

    // Autores sin libros: $lookup y luego filtrar los que quedaron con el arreglo vacio.
    @Aggregation(pipeline = {
            "{ $lookup: { from: 'book', localField: '_id', foreignField: 'author', as: 'bookDocs' } }",
            "{ $match: { bookDocs: { $size: 0 } } }",
            "{ $unset: 'bookDocs' }"
    })
    List<Author> findWithoutBooks();

    // Proyeccion a un DTO: $project arma un documento con la forma del record AuthorBookCount.
    // $toString y $toLong convierten los tipos BSON a los de los componentes del record.
    @Aggregation(pipeline = {
            "{ $lookup: { from: 'book', localField: '_id', foreignField: 'author', as: 'bookDocs' } }",
            "{ $project: { _id: 0, id: { $toString: '$_id' }, fullname: 1, books: { $toLong: { $size: '$bookDocs' } } } }",
            "{ $sort: { books: -1, fullname: 1 } }"
    })
    List<AuthorBookCount> countBooksByAuthor();
}
