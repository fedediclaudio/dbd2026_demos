package com.dbd26.demo1.library.repositories;

import com.dbd26.demo1.library.model.Book;
import com.dbd26.demo1.library.model.DigitalBook;

import java.util.List;
import java.util.Optional;

public interface BookRepository {

    void save(Book book);

    void delete(Book book);

    List<Book> findAll();

    List<DigitalBook> findAllDigitalBooks();

    List<DigitalBook> findDigitalBooksByFormat(String format);

    List<Book> findByType(Class<? extends Book> type);

    Optional<Book> findByIsbn(String isbn);

    Optional<Book> findByIsbnNative(String isbn);

    List<Book> findByAuthorId(Long authorId);

    List<Book> findByAuthorFullname(String fullname);

    List<Book> findByYearBetween(int from, int to);

    List<Book> searchByTitle(String text);

    Double averageYearByAuthor(Long authorId);

    int updateComments(String isbn, String comments);
}
