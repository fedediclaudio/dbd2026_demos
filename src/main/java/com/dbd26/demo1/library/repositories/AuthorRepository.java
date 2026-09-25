package com.dbd26.demo1.library.repositories;

import com.dbd26.demo1.library.dto.AuthorBookCount;
import com.dbd26.demo1.library.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    // Consulta derivada: Spring Data genera el JPQL a partir del nombre del metodo.
    List<Author> findByDateOfBirthGreaterThan(LocalDate date);

    // Consulta derivada con "like %texto%" insensible a mayusculas.
    List<Author> findByFullnameContainingIgnoreCase(String text);

    // JPQL con join, agrupamiento y funcion de agregacion: autores ordenados por cantidad de libros.
    // Solo devuelve autores que tienen al menos un libro (join interno).
    @Query("select a from Author a join a.books b group by a.id order by count(b) desc")
    List<Author> findByMoreBooks();

    // JOIN FETCH: trae el autor y sus libros en UNA sola consulta, en lugar de la carga LAZY posterior
    // (evita el problema "N+1": 1 consulta por el autor + N consultas por cada acceso a la coleccion).
    @Query("select a from Author a left join fetch a.books where a.id = :id")
    Optional<Author> findByIdWithBooks(@Param("id") Long id);

    // Predicado "is empty" sobre una coleccion (equivale a un "not exists" en SQL).
    @Query("select a from Author a where a.books is empty")
    List<Author> findWithoutBooks();

    // Proyeccion con expresion constructora: el resultado no es una entidad sino un DTO (record).
    // "left join" para que aparezcan tambien los autores con cero libros.
    @Query("select new com.dbd26.demo1.library.dto.AuthorBookCount(a.id, a.fullname, count(b)) " +
           "from Author a left join a.books b " +
           "group by a.id, a.fullname " +
           "order by count(b) desc, a.fullname")
    List<AuthorBookCount> countBooksByAuthor();

}
