package com.dbd26.demo1.library.services;

import com.dbd26.demo1.library.LibraryException;
import com.dbd26.demo1.library.model.Author;
import com.dbd26.demo1.library.model.Book;
import com.dbd26.demo1.library.model.DigitalBook;
import com.dbd26.demo1.library.model.PhysicalBook;
import com.dbd26.demo1.library.repositories.AuthorRepository;
import com.dbd26.demo1.library.repositories.BookRepository;
import com.dbd26.demo1.library.repositories.DigitalBookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private DigitalBookRepository digitalBookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Override
    @Transactional
    public Book createBook(Book book) throws LibraryException {
        if (book.getAuthor() == null || book.getAuthor().getId() == null) {
            throw new LibraryException("El libro debe indicar el id de su autor");
        }
        Long authorId = book.getAuthor().getId();
        Author author = this.authorRepository.findById(authorId)
                .orElseThrow(() -> new LibraryException("No existe el autor con id " + authorId));
        // Se valida antes de insertar para dar un mensaje claro; si no, la restriccion "unique" del ISBN
        // haria fallar el INSERT con una DataIntegrityViolationException.
        if (this.bookRepository.findByIsbn(book.getIsbn()).isPresent()) {
            throw new LibraryException("Ya existe un libro con ISBN " + book.getIsbn());
        }
        book.setAuthor(author);
        // Se mantiene coherente el lado inverso de la relacion en memoria (la FK la escribe el lado Book).
        author.getBooks().add(book);
        return this.bookRepository.save(book);
    }

    @Override
    @Transactional
    public PhysicalBook createPhysicalBook(String isbn, String title, int year, String comments, float weight, String location, Author author) throws LibraryException {
        return (PhysicalBook) this.createBook(new PhysicalBook(isbn, title, year, comments, weight, location, author));
    }

    @Override
    @Transactional
    public DigitalBook createDigitalBook(String isbn, String title, int year, String comments, String format, float size, Author author) throws LibraryException {
        return (DigitalBook) this.createBook(new DigitalBook(isbn, title, year, comments, format, size, author));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> findAll() {
        return this.bookRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DigitalBook> findAllDigitalBook() {
        return this.digitalBookRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DigitalBook> findDigitalBooksByFormat(String format) {
        return this.digitalBookRepository.findByFormatIgnoreCase(format);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> findByType(Class<? extends Book> type) {
        return this.bookRepository.findByType(type);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Book> findByIsbn(String isbn) {
        return this.bookRepository.findByIsbn(isbn);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Book> findByIsbnNative(String isbn) {
        return this.bookRepository.findByIsbnNative(isbn);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> findByAuthor(Author author) {
        return this.bookRepository.findByAuthorId(author.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> findByAuthorFullname(String fullname) {
        return this.bookRepository.findByAuthorFullname(fullname);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> findByYearBetween(int from, int to) {
        return this.bookRepository.findByYearBetween(from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> searchByTitle(String text) {
        return this.bookRepository.searchByTitle(text);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageYearByAuthor(Author author) {
        return this.bookRepository.averageYearByAuthor(author.getId());
    }

    @Override
    @Transactional
    public boolean updateComments(String isbn, String comments) {
        return this.bookRepository.updateComments(isbn, comments) == 1;
    }

    @Override
    @Transactional
    public boolean deleteByIsbn(String isbn) {
        Optional<Book> bookOptional = this.bookRepository.findByIsbn(isbn);
        if (bookOptional.isPresent()) {
            Book book = bookOptional.get();
            book.getAuthor().getBooks().remove(book);
            this.bookRepository.delete(book);
            return true;
        } else return false;
    }
}
