package com.dbd26.demo1.library.services;

import com.dbd26.demo1.library.LibraryException;
import com.dbd26.demo1.library.dto.AuthorBookCount;
import com.dbd26.demo1.library.model.Author;
import com.dbd26.demo1.library.repositories.AuthorRepository;
import com.dbd26.demo1.library.repositories.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Sin @Transactional ni dirty checking: cada save() o delete() impacta en la base en el momento.
@Service
public class AuthorServiceImpl implements AuthorService {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Override
    public Author createAuthor(String fullname, LocalDate dateOfBirth) throws LibraryException {
        if (fullname == null || fullname.isBlank()) {
            throw new LibraryException("El nombre del autor es obligatorio");
        }
        // save() sin id hace un insert y completa el id generado.
        return this.authorRepository.save(new Author(fullname, dateOfBirth));
    }

    @Override
    public Author updateAuthor(String id, String fullname, LocalDate dateOfBirth) throws LibraryException {
        Optional<Author> authorOptional = this.authorRepository.findById(id);
        if (authorOptional.isPresent()) {
            Author author = authorOptional.get();
            author.setFullname(fullname);
            author.setDateOfBirth(dateOfBirth);
            // El cambio no se persiste solo: hay que llamar a save().
            return this.authorRepository.save(author);
        } else return null;
    }

    @Override
    public Author updateAuthor(Author author) throws LibraryException {
        if (author.getId() == null || !this.authorRepository.existsById(author.getId())) {
            throw new LibraryException("No existe el autor con id " + author.getId());
        }
        // save() con id y version: update con filtro { _id, version }. Version vieja => OptimisticLockingFailureException.
        return this.authorRepository.save(author);
    }

    @Override
    public boolean deleteAuthor(String id) throws LibraryException {
        Optional<Author> authorOptional = this.authorRepository.findById(id);
        if (authorOptional.isPresent()) {
            Author author = authorOptional.get();
            // Sin claves foraneas: la integridad referencial la controla la aplicacion.
            if (!this.bookRepository.findByAuthor(author).isEmpty()) {
                throw new LibraryException("No se puede eliminar el autor " + author.getFullname() + " porque tiene libros");
            }
            this.authorRepository.delete(author);
            return true;
        } else return false;
    }

    @Override
    public Optional<Author> getById(String id) {
        return this.authorRepository.findById(id);
    }

    @Override
    public Optional<Author> getByIdWithBooks(String id) {
        // No hay join fetch: getBooks() dispara el lookup declarado en Author.books (segunda consulta).
        Optional<Author> author = this.authorRepository.findById(id);
        author.ifPresent(a -> a.getBooks().size());
        return author;
    }

    @Override
    public List<Author> getListOfAuthors() {
        return this.authorRepository.findAll();
    }

    @Override
    public List<Author> searchByName(String text) {
        return this.authorRepository.findByFullnameContainingIgnoreCase(text);
    }

    @Override
    public List<Author> getYoungAuthors() {
        LocalDate date = LocalDate.now().minusYears(60);
        return this.authorRepository.findByDateOfBirthGreaterThan(date);
    }

    @Override
    public Author getAuthorWithMoreBooks() {
        List<Author> authors = this.authorRepository.findByMoreBooks();
        return authors.isEmpty() ? null : authors.get(0);
    }

    @Override
    public List<Author> getAuthorsWithoutBooks() {
        return this.authorRepository.findWithoutBooks();
    }

    @Override
    public List<AuthorBookCount> getBookCountByAuthor() {
        return this.authorRepository.countBooksByAuthor();
    }
}
