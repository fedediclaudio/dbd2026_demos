package com.dbd26.demo1.library.repositories;

import com.dbd26.demo1.library.dto.AuthorBookCount;
import com.dbd26.demo1.library.model.Author;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

// MongoRepository: como JpaRepository, pero las consultas son filtros JSON (@Query) o pipelines (@Aggregation).
@Repository
public interface AuthorRepository extends MongoRepository<Author, String> {

    // Consulta derivada: { date_of_birth: { $gt: ?0 } }
    List<Author> findByDateOfBirthGreaterThan(LocalDate date);

    // Consulta derivada: { fullname: /texto/i }
    List<Author> findByFullnameContainingIgnoreCase(String text);

    // No hay join: $lookup trae los libros de cada autor. $unset quita los campos auxiliares antes de mapear a Author.
    @Aggregation(pipeline = {
            "{ $lookup: { from: 'book', localField: '_id', foreignField: 'author', as: 'bookDocs' } }",
            "{ $addFields: { bookCount: { $size: '$bookDocs' } } }",
            "{ $match: { bookCount: { $gt: 0 } } }",
            "{ $sort: { bookCount: -1 } }",
            "{ $unset: ['bookDocs', 'bookCount'] }"
    })
    List<Author> findByMoreBooks();

    // Autores sin libros: el arreglo del $lookup queda vacio.
    @Aggregation(pipeline = {
            "{ $lookup: { from: 'book', localField: '_id', foreignField: 'author', as: 'bookDocs' } }",
            "{ $match: { bookDocs: { $size: 0 } } }",
            "{ $unset: 'bookDocs' }"
    })
    List<Author> findWithoutBooks();

    // Proyeccion a un DTO: $project arma un documento con la forma del record AuthorBookCount.
    @Aggregation(pipeline = {
            "{ $lookup: { from: 'book', localField: '_id', foreignField: 'author', as: 'bookDocs' } }",
            "{ $project: { _id: 0, id: { $toString: '$_id' }, fullname: 1, books: { $toLong: { $size: '$bookDocs' } } } }",
            "{ $sort: { books: -1, fullname: 1 } }"
    })
    List<AuthorBookCount> countBooksByAuthor();
}
