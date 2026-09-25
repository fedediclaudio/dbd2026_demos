package com.dbd26.demo1.library;

import com.dbd26.demo1.library.model.Author;
import com.dbd26.demo1.library.services.AuthorService;
import com.dbd26.demo1.library.services.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/*
 A diferencia de LibraryApplicationTests, esta clase NO tiene @Transactional: cada llamada a un servicio
 corre en su propia transaccion y hace commit al volver. Eso permite mostrar dos situaciones que solo ocurren
 entre transacciones distintas: el bloqueo optimista (@Version) y una violacion de clave foranea al hacer commit.
 */
@SpringBootTest
class LibraryConcurrencyTests {

	@Autowired
	private AuthorService authorService;

	@Autowired
	private BookService bookService;

	@Test
	void optimisticLockingDetectsConcurrentUpdate() throws LibraryException {
		Long id = this.authorService.createAuthor("Antonio Di Benedetto", LocalDate.of(1922, 11, 2)).getId();

		// Dos "usuarios" leen el mismo autor. Al terminar cada transaccion las copias quedan "detached",
		// ambas con version = 0.
		Author user1 = this.authorService.getById(id).orElseThrow();
		Author user2 = this.authorService.getById(id).orElseThrow();
		assertEquals(0, user1.getVersion());
		assertEquals(0, user2.getVersion());

		// El primero guarda: Hibernate hace "update ... set version=1 where id=? and version=0" -> OK
		user1.setFullname("Antonio Di Benedetto (Mendoza)");
		Author saved = this.authorService.updateAuthor(user1);
		assertEquals(1, saved.getVersion());

		// El segundo guarda con la version vieja (0): la fila ya tiene version 1, Hibernate detecta el conflicto
		user2.setFullname("Otro nombre");
		assertThrows(OptimisticLockingFailureException.class, () -> this.authorService.updateAuthor(user2));

		// En la base quedo el cambio del primero
		assertEquals("Antonio Di Benedetto (Mendoza)", this.authorService.getById(id).orElseThrow().getFullname());
	}

	@Test
	void deletingAuthorWithBooksViolatesForeignKey() throws LibraryException {
		Author author = this.authorService.createAuthor("Olga Orozco", LocalDate.of(1920, 3, 17));
		this.bookService.createPhysicalBook("9782000000001", "Las muertes", 1952, null, 0.2f, "Seccion O", author);

		// La FK book.id_author (not null, sin cascade) impide borrar un autor con libros.
		// El DELETE falla al hacer commit y Spring lo traduce a DataIntegrityViolationException.
		assertThrows(DataIntegrityViolationException.class, () -> this.authorService.deleteAuthor(author.getId()));
		assertTrue(this.authorService.getById(author.getId()).isPresent());

		// Borrando primero el libro, el autor se puede eliminar
		assertTrue(this.bookService.deleteByIsbn("9782000000001"));
		assertTrue(this.authorService.deleteAuthor(author.getId()));
		assertTrue(this.authorService.getById(author.getId()).isEmpty());
	}
}
