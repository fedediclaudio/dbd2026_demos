package com.dbd26.demo1.library.services;

import com.dbd26.demo1.library.LibraryException;
import com.dbd26.demo1.library.dto.AuthorBookCount;
import com.dbd26.demo1.library.model.Author;
import com.dbd26.demo1.library.repositories.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AuthorServiceImpl implements AuthorService {

    @Autowired
    private AuthorRepository authorRepository;

    @Override
    @Transactional
    public Author createAuthor(String fullname, LocalDate dateOfBirth) throws LibraryException {
        if (fullname == null || fullname.isBlank()) {
            throw new LibraryException("El nombre del autor es obligatorio");
        }
        Author author = new Author(fullname, dateOfBirth);
        this.authorRepository.save(author);
        return author;
    }

    @Override
    @Transactional
    public Author updateAuthor(Long id, String fullname, LocalDate dateOfBirth) throws LibraryException {
        Optional<Author> authorOptional = this.authorRepository.findById(id);
        if (authorOptional.isPresent()) {
            Author author = authorOptional.get();
            author.setFullname(fullname);
            author.setDateOfBirth(dateOfBirth);
            // No hace falta llamar a save(): la entidad esta "managed" y Hibernate detecta los cambios
            // (dirty checking) y genera el UPDATE al terminar la transaccion.
            return author;
        } else return null;
    }

    @Override
    @Transactional
    public Author updateAuthor(Author author) throws LibraryException {
        if (author.getId() == null || !this.authorRepository.existsById(author.getId())) {
            throw new LibraryException("No existe el autor con id " + author.getId());
        }
        // save() sobre una entidad "detached" (viene del JSON) hace un merge: copia el estado a la entidad
        // administrada. Si la "version" recibida es vieja, falla con OptimisticLockException.
        return this.authorRepository.save(author);
    }

    @Override
    @Transactional
    public boolean deleteAuthor(Long id) throws LibraryException {
        Optional<Author> authorOptional = this.authorRepository.findById(id);
        if (authorOptional.isPresent()) {
            Author author = authorOptional.get();
            // Si el autor tiene libros, la FK id_author (not null) impide el borrado: DataIntegrityViolationException.
            this.authorRepository.delete(author);
            return true;
        } else return false;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Author> getById(Long id) {
        return this.authorRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Author> getByIdWithBooks(Long id) {
        return this.authorRepository.findByIdWithBooks(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Author> getListOfAuthors() {
        return this.authorRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Author> searchByName(String text) {
        return this.authorRepository.findByFullnameContainingIgnoreCase(text);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Author> getYoungAuthors() {
        LocalDate date = LocalDate.now().minusYears(60);
        return this.authorRepository.findByDateOfBirthGreaterThan(date);
    }

    @Override
    @Transactional(readOnly = true)
    public Author getAuthorWithMoreBooks() {
        List<Author> authors = this.authorRepository.findByMoreBooks();
        if (!authors.isEmpty()) {
            Author a = authors.get(0);
            // Fuerza la carga LAZY de la coleccion mientras la transaccion sigue abierta.
            a.getBooks().size();
            return a;
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Author> getAuthorsWithoutBooks() {
        return this.authorRepository.findWithoutBooks();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuthorBookCount> getBookCountByAuthor() {
        return this.authorRepository.countBooksByAuthor();
    }
}
