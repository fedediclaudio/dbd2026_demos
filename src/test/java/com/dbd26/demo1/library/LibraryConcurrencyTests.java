package com.dbd26.demo1.library;

import com.dbd26.demo1.library.model.Author;
import com.dbd26.demo1.library.services.AuthorService;
import com.dbd26.demo1.library.services.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/*
 Las dos situaciones que en las ramas relacionales dependian de transacciones distintas. En MongoDB no hay
 transacciones de por medio, pero el comportamiento observable es el mismo para el bloqueo optimista
 y distinto para la integridad referencial, que MongoDB no controla.
 */
@SpringBootTest
class LibraryConcurrencyTests {

	@Autowired
	private AuthorService authorService;

	@Autowired
	private BookService bookService;

	@Test
	void optimisticLockingDetectsConcurrentUpdate() throws LibraryException {
		String id = this.authorService.createAuthor("Antonio Di Benedetto", LocalDate.of(1922, 11, 2)).getId();

		// Dos "usuarios" leen el mismo autor: dos objetos distintos, ambos con version = 0.
		Author user1 = this.authorService.getById(id).orElseThrow();
		Author user2 = this.authorService.getById(id).orElseThrow();
		assertEquals(0, user1.getVersion().intValue());
		assertEquals(0, user2.getVersion().intValue());

		// El primero guarda: updateOne({ _id: ..., version: 0 }, { $set: {...}, $inc: { version: 1 } }) -> OK
		user1.setFullname("Antonio Di Benedetto (Mendoza)");
		Author saved = this.authorService.updateAuthor(user1);
		assertEquals(1, saved.getVersion().intValue());

		// El segundo guarda con la version vieja (0): el filtro no coincide con ningun documento -> conflicto
		user2.setFullname("Otro nombre");
		assertThrows(OptimisticLockingFailureException.class, () -> this.authorService.updateAuthor(user2));

		// En la base quedo el cambio del primero
		assertEquals("Antonio Di Benedetto (Mendoza)", this.authorService.getById(id).orElseThrow().getFullname());
	}

	@Test
	void deletingAuthorWithBooksIsRejectedByTheApplication() throws LibraryException {
		Author author = this.authorService.createAuthor("Olga Orozco", LocalDate.of(1920, 3, 17));
		this.bookService.createPhysicalBook("9782000000001", "Las muertes", 1952, null, 0.2f, "Seccion O", author);

		// MongoDB no tiene claves foraneas: el control lo hace el servicio, no la base.
		LibraryException e = assertThrows(LibraryException.class, () -> this.authorService.deleteAuthor(author.getId()));
		assertTrue(e.getMessage().contains("Olga Orozco"));
		assertTrue(this.authorService.getById(author.getId()).isPresent());

		// Borrando primero el libro, el autor se puede eliminar
		assertTrue(this.bookService.deleteByIsbn("9782000000001"));
		assertTrue(this.authorService.deleteAuthor(author.getId()));
		assertTrue(this.authorService.getById(author.getId()).isEmpty());
	}
}
