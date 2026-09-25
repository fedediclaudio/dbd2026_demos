package com.dbd26.demo1.library.repositories;

import com.dbd26.demo1.library.dto.AuthorBookCount;
import com.dbd26.demo1.library.model.Author;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/*
 A diferencia de la rama jpa_repositories (donde esta interfaz extiende JpaRepository y Spring Data genera la
 implementacion), aca la implementacion se escribe a mano en AuthorRepositoryImpl usando la Session de Hibernate.
 */
public interface AuthorRepository {

    void save(Author author);

    Author update(Author author);

    void delete(Author author);

    Optional<Author> findById(Long id);

    boolean existsById(Long id);

    List<Author> findAll();

    List<Author> findByDateOfBirthGreaterThan(LocalDate date);

    List<Author> findByFullnameContainingIgnoreCase(String text);

    List<Author> findByMoreBooks();

    Optional<Author> findByIdWithBooks(Long id);

    List<Author> findWithoutBooks();

    List<AuthorBookCount> countBooksByAuthor();
}
