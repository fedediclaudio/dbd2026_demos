package com.dbd26.demo1.library.repositories;

import com.dbd26.demo1.library.model.Author;
import com.dbd26.demo1.library.model.Book;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends MongoRepository<Book, String> {

    // Consulta derivada: { isbn: ?0 }
    Optional<Book> findByIsbn(String isbn);

    // Consulta derivada sobre la referencia: compara el campo "author" con el _id del autor.
    List<Book> findByAuthor(Author author);

    // @Query con el filtro JSON de MongoDB, igual que en mongosh: db.book.find({ isbn: ... }).
    @Query("{ 'isbn': ?0 }")
    Optional<Book> findByIsbnNative(String isbn);

    // Rango inclusivo con orden (el Between derivado es exclusivo).
    @Query(value = "{ 'year': { $gte: ?0, $lte: ?1 } }", sort = "{ 'year': 1, 'title': 1 }")
    List<Book> findByYearBetween(int from, int to);

    // Busqueda parcial, insensible a mayusculas (equivale a like + lower).
    @Query("{ 'title': { $regex: ?0, $options: 'i' } }")
    List<Book> searchByTitle(String text);

    // Consulta polimorfica: filtra por el discriminador "_class".
    @Query("{ '_class': ?0 }")
    List<Book> findByTypeAlias(String typeAlias);

    // Filtrar por un campo del autor requiere $lookup: el libro solo guarda su _id.
    @Aggregation(pipeline = {
            "{ $lookup: { from: 'author', localField: 'author', foreignField: '_id', as: 'authorDoc' } }",
            "{ $match: { 'authorDoc.fullname': ?0 } }",
            "{ $unset: 'authorDoc' }"
    })
    List<Book> findByAuthorFullname(String fullname);

    // Agregacion escalar con $avg. Devuelve null si el autor no tiene libros.
    // En un pipeline el parametro se inserta tal cual, por eso se recibe el ObjectId y no el Author.
    @Aggregation(pipeline = {
            "{ $match: { 'author': ?0 } }",
            "{ $group: { _id: null, averageYear: { $avg: '$year' } } }"
    })
    Double averageYearByAuthor(ObjectId authorId);
}
