package com.dbd26.demo1.library.repositories;

import com.dbd26.demo1.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Consultas derivadas del nombre del metodo.
    Optional<Book> findByIsbn(String isbn);

    List<Book> findByAuthorId(Long id);

    // JPQL con parametros nombrados (:from, :to) y "between".
    @Query("select b from Book b where b.year between :from and :to order by b.year, b.title")
    List<Book> findByYearBetween(@Param("from") int from, @Param("to") int to);

    // Busqueda parcial con "like" + "concat", y "join fetch" del autor (aunque es EAGER, asi queda en la misma consulta).
    @Query("select b from Book b join fetch b.author where lower(b.title) like lower(concat('%', :text, '%'))")
    List<Book> searchByTitle(@Param("text") String text);

    // Navegacion por asociacion (b.author.fullname): Hibernate genera el join con la tabla author automaticamente.
    @Query("select b from Book b where b.author.fullname = :fullname")
    List<Book> findByAuthorFullname(@Param("fullname") String fullname);

    // Consulta polimorfica: "type(b)" filtra por subclase. En SINGLE_TABLE se traduce a un filtro por la columna
    // discriminadora "type". El parametro recibe la clase (PhysicalBook.class o DigitalBook.class).
    @Query("select b from Book b where type(b) = :type")
    List<Book> findByType(@Param("type") Class<? extends Book> type);

    // Funcion de agregacion escalar: el resultado no es una entidad sino un unico valor.
    @Query("select avg(b.year) from Book b where b.author.id = :authorId")
    Double averageYearByAuthor(@Param("authorId") Long authorId);

    // Operacion masiva (bulk update) con @Modifying: se ejecuta directo en la base, sin pasar por las entidades
    // en memoria. flushAutomatically: envia los cambios pendientes antes; clearAutomatically: limpia el contexto de
    // persistencia despues, para que las entidades ya cargadas no queden desactualizadas respecto de la base.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Book b set b.comments = :comments where b.isbn = :isbn")
    int updateComments(@Param("isbn") String isbn, @Param("comments") String comments);

    // Consulta SQL nativa (no JPQL): se escribe contra las tablas y columnas reales, no contra las entidades.
    // Hibernate igualmente mapea las filas a la entidad (usando la columna discriminadora para elegir la subclase).
    @Query(value = "select * from book where isbn = :isbn", nativeQuery = true)
    Optional<Book> findByIsbnNative(@Param("isbn") String isbn);

}
