package com.dbd26.demo1.library.services;

import com.dbd26.demo1.library.LibraryException;
import com.dbd26.demo1.library.dto.AuthorBookCount;
import com.dbd26.demo1.library.model.Author;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AuthorService {

    Author createAuthor(String fullname, LocalDate dateOfBirth) throws LibraryException;

    Author updateAuthor(Long id, String fullname, LocalDate dateOfBirth) throws LibraryException;

    Author updateAuthor(Author author) throws LibraryException;

    boolean deleteAuthor(Long id) throws LibraryException;

    Optional<Author> getById(Long id);

    Optional<Author> getByIdWithBooks(Long id);

    List<Author> getListOfAuthors();

    List<Author> searchByName(String text);

    List<Author> getYoungAuthors();

    Author getAuthorWithMoreBooks();

    List<Author> getAuthorsWithoutBooks();

    List<AuthorBookCount> getBookCountByAuthor();
}
