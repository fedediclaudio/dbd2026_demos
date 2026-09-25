package com.dbd26.demo1.library.services;

import com.dbd26.demo1.library.LibraryException;
import com.dbd26.demo1.library.model.Author;
import com.dbd26.demo1.library.model.Book;
import com.dbd26.demo1.library.model.DigitalBook;
import com.dbd26.demo1.library.model.PhysicalBook;

import java.util.List;
import java.util.Optional;

public interface BookService {

    /** Guarda un libro (fisico o digital) ya construido. El autor se resuelve por su id. */
    Book createBook(Book book) throws LibraryException;

    PhysicalBook createPhysicalBook(String isbn, String title, int year, String comments, float weight, String location, Author author) throws LibraryException;

    DigitalBook createDigitalBook(String isbn, String title, int year, String comments, String format, float size, Author author) throws LibraryException;

    List<Book> findAll();

    List<DigitalBook> findAllDigitalBook();

    List<DigitalBook> findDigitalBooksByFormat(String format);

    List<Book> findByType(Class<? extends Book> type);

    Optional<Book> findByIsbn(String isbn);

    Optional<Book> findByIsbnNative(String isbn);

    List<Book> findByAuthor(Author author);

    List<Book> findByAuthorFullname(String fullname);

    List<Book> findByYearBetween(int from, int to);

    List<Book> searchByTitle(String text);

    Double getAverageYearByAuthor(Author author);

    boolean updateComments(String isbn, String comments);

    boolean deleteByIsbn(String isbn);
}
