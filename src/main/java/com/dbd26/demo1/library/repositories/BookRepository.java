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

/*
 Ademas de los metodos generados por Spring Data, este repositorio extiende BookRepositoryCustom: un "fragmento"
 con una implementacion escrita a mano sobre MongoTemplate (ver BookRepositoryImpl).
 */
@Repository
public interface BookRepository extends MongoRepository<Book, String>, BookRepositoryCustom {

    // Consulta derivada: { isbn: ?0 }
    Optional<Book> findByIsbn(String isbn);

    // Consulta derivada sobre la referencia: Spring Data compara el campo "author" con el _id del autor recibido.
    List<Book> findByAuthor(Author author);

    // @Query con el filtro JSON "nativo" de MongoDB: es lo mismo que se escribiria en mongosh en db.book.find(...).
    // Equivale a la consulta SQL nativa de las otras ramas.
    @Query("{ 'isbn': ?0 }")
    Optional<Book> findByIsbnNative(String isbn);

    // Rango con $gte / $lte y orden. (El "Between" derivado de Spring Data MongoDB es exclusivo, por eso @Query.)
    @Query(value = "{ 'year': { $gte: ?0, $lte: ?1 } }", sort = "{ 'year': 1, 'title': 1 }")
    List<Book> findByYearBetween(int from, int to);

    // Busqueda parcial con expresion regular insensible a mayusculas (equivale a like + lower).
    @Query("{ 'title': { $regex: ?0, $options: 'i' } }")
    List<Book> searchByTitle(String text);

    // Consulta polimorfica: filtra por el campo discriminador "_class" ("Physical" o "Digital").
    @Query("{ '_class': ?0 }")
    List<Book> findByTypeAlias(String typeAlias);

    // Filtrar por un campo del autor: el documento book solo tiene el _id del autor, asi que hace falta
    // un $lookup para traer el autor y poder comparar su nombre (equivale a b.author.fullname en JPQL).
    @Aggregation(pipeline = {
            "{ $lookup: { from: 'author', localField: 'author', foreignField: '_id', as: 'authorDoc' } }",
            "{ $match: { 'authorDoc.fullname': ?0 } }",
            "{ $unset: 'authorDoc' }"
    })
    List<Book> findByAuthorFullname(String fullname);

    // Agregacion escalar: $group sobre todos los libros del autor y $avg. Devuelve null si no tiene libros.
    // En un pipeline los parametros se insertan tal cual: el campo "author" guarda un ObjectId, asi que hay que
    // pasar el id ya convertido (en las consultas derivadas, como findByAuthor, Spring Data lo convierte solo).
    @Aggregation(pipeline = {
            "{ $match: { 'author': ?0 } }",
            "{ $group: { _id: null, averageYear: { $avg: '$year' } } }"
    })
    Double averageYearByAuthor(ObjectId authorId);
}
