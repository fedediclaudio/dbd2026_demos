package com.dbd26.demo1.library.services;

import com.dbd26.demo1.library.LibraryException;
import com.dbd26.demo1.library.model.Author;
import com.dbd26.demo1.library.model.Book;
import com.dbd26.demo1.library.model.DigitalBook;
import com.dbd26.demo1.library.model.PhysicalBook;
import com.dbd26.demo1.library.repositories.AuthorRepository;
import com.dbd26.demo1.library.repositories.BookRepository;
import com.dbd26.demo1.library.repositories.DigitalBookRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.stereotype.Service;

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
    public Book createBook(Book book) throws LibraryException {
        if (book.getAuthor() == null || book.getAuthor().getId() == null) {
            throw new LibraryException("El libro debe indicar el id de su autor");
        }
        String authorId = book.getAuthor().getId();
        Author author = this.authorRepository.findById(authorId)
                .orElseThrow(() -> new LibraryException("No existe el autor con id " + authorId));
        // Se valida antes de insertar para dar un mensaje claro; si no, el indice unico sobre isbn
        // haria fallar el insert con una DuplicateKeyException.
        if (this.bookRepository.findByIsbn(book.getIsbn()).isPresent()) {
            throw new LibraryException("Ya existe un libro con ISBN " + book.getIsbn());
        }
        book.setAuthor(author);
        // No hay que mantener author.getBooks(): esa lista no se guarda, se calcula al leer (ver Author.books).
        return this.bookRepository.save(book);
    }

    @Override
    public PhysicalBook createPhysicalBook(String isbn, String title, int year, String comments, float weight, String location, Author author) throws LibraryException {
        return (PhysicalBook) this.createBook(new PhysicalBook(isbn, title, year, comments, weight, location, author));
    }

    @Override
    public DigitalBook createDigitalBook(String isbn, String title, int year, String comments, String format, float size, Author author) throws LibraryException {
        return (DigitalBook) this.createBook(new DigitalBook(isbn, title, year, comments, format, size, author));
    }

    @Override
    public List<Book> findAll() {
        return this.bookRepository.findAll();
    }

    @Override
    public List<DigitalBook> findAllDigitalBook() {
        return this.digitalBookRepository.findAllDigital();
    }

    @Override
    public List<DigitalBook> findDigitalBooksByFormat(String format) {
        return this.digitalBookRepository.findByFormatIgnoreCase(format);
    }

    @Override
    public List<Book> findByType(Class<? extends Book> type) {
        // El valor del discriminador "_class" es el @TypeAlias de la subclase.
        TypeAlias alias = type.getAnnotation(TypeAlias.class);
        return this.bookRepository.findByTypeAlias(alias != null ? alias.value() : type.getName());
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return this.bookRepository.findByIsbn(isbn);
    }

    @Override
    public Optional<Book> findByIsbnNative(String isbn) {
        return this.bookRepository.findByIsbnNative(isbn);
    }

    @Override
    public List<Book> findByAuthor(Author author) {
        return this.bookRepository.findByAuthor(author);
    }

    @Override
    public List<Book> findByAuthorFullname(String fullname) {
        return this.bookRepository.findByAuthorFullname(fullname);
    }

    @Override
    public List<Book> findByYearBetween(int from, int to) {
        return this.bookRepository.findByYearBetween(from, to);
    }

    @Override
    public List<Book> searchByTitle(String text) {
        return this.bookRepository.searchByTitle(text);
    }

    @Override
    public Double getAverageYearByAuthor(Author author) {
        // El id String de Java (24 caracteres hexadecimales) se convierte al ObjectId que guarda MongoDB.
        return this.bookRepository.averageYearByAuthor(new ObjectId(author.getId()));
    }

    @Override
    public boolean updateComments(String isbn, String comments) {
        // Sin contexto de persistencia ni "dirty checking": se lee el documento, se modifica y se vuelve a guardar.
        // (MongoDB tambien permite modificar sin leer, con updateOne y $set, pero eso requiere MongoTemplate.)
        Optional<Book> bookOptional = this.bookRepository.findByIsbn(isbn);
        if (bookOptional.isPresent()) {
            Book book = bookOptional.get();
            book.setComments(comments);
            this.bookRepository.save(book);
            return true;
        } else return false;
    }

    @Override
    public boolean deleteByIsbn(String isbn) {
        Optional<Book> bookOptional = this.bookRepository.findByIsbn(isbn);
        if (bookOptional.isPresent()) {
            this.bookRepository.delete(bookOptional.get());
            return true;
        } else return false;
    }
}
