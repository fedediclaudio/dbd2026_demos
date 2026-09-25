package com.dbd26.demo1.library;

import com.dbd26.demo1.library.dto.AuthorBookCount;
import com.dbd26.demo1.library.model.Author;
import com.dbd26.demo1.library.model.Book;
import com.dbd26.demo1.library.model.DigitalBook;
import com.dbd26.demo1.library.model.PhysicalBook;
import com.dbd26.demo1.library.services.AuthorService;
import com.dbd26.demo1.library.services.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/*
 Todos los tests corren contra la misma base y, por @Rollback(false), los datos quedan persistidos
 (util para inspeccionarlos en MySQL despues de correrlos). Por eso:
  - las comprobaciones de cantidades son relativas (se cuenta antes y despues), nunca absolutas;
  - cada test usa ISBN propios, para no chocar con la restriccion "unique";
  - las fechas se calculan a partir de LocalDate.now(), para que no dejen de valer con el paso del tiempo.
 */
@SpringBootTest
@Transactional
@Rollback(false)
class LibraryApplicationTests {

	@Autowired
	private AuthorService authorService;

	@Autowired
	private BookService bookService;

	@Test
	void contextLoads() {
	}

	@Test
	void createAndGetAuthorTest() throws LibraryException {
		Author newAuthor = this.authorService.createAuthor("Julio Cortázar", LocalDate.of(1914, 8, 26));
		assertNotNull(newAuthor);
		Optional<Author> authorOptional = this.authorService.getById(newAuthor.getId());
		assertTrue(authorOptional.isPresent());
		Author author = authorOptional.get();
		assertEquals(newAuthor.getId(), author.getId());
		assertEquals("Julio Cortázar", author.getFullname());
	}

	@Test
	void createAuthorWithoutNameFails() {
		assertThrows(LibraryException.class, () -> this.authorService.createAuthor("  ", LocalDate.of(1914, 8, 26)));
	}

	@Test
	void createAndGetBooksTest() throws LibraryException {
		Author author = this.authorService.createAuthor("Julio Cortázar", LocalDate.of(1914, 8, 26));
		this.bookService.createPhysicalBook("9780000000011", "Rayuela", 1963, "Comentario", 200, "Seccion B", author);
		Optional<Book> optionalBook = this.bookService.findByIsbn("9780000000011");
		assertTrue(optionalBook.isPresent());
		Book book = optionalBook.get();
		assertEquals("Rayuela", book.getTitle());
		assertEquals("Julio Cortázar", book.getAuthor().getFullname());
		assertInstanceOf(PhysicalBook.class, book);
	}

	@Test
	void createBookWithDuplicatedIsbnFails() throws LibraryException {
		Author author = this.authorService.createAuthor("Julio Cortázar", LocalDate.of(1914, 8, 26));
		this.bookService.createPhysicalBook("9780000000012", "Bestiario", 1951, "Comentario", 150, "Seccion B", author);
		LibraryException e = assertThrows(LibraryException.class,
				() -> this.bookService.createDigitalBook("9780000000012", "Casa tomada", 1946, "Comentario", "PDF", 1.0f, author));
		assertTrue(e.getMessage().contains("9780000000012"));
	}

	@Test
	void updateAuthor() throws LibraryException {
		Author author = this.authorService.createAuthor("Julioo Cortázar", LocalDate.of(1914, 8, 26));
		this.authorService.updateAuthor(author.getId(), "Julio Cortázar", author.getDateOfBirth());
		Author updatedAuthor = this.authorService.getById(author.getId()).orElse(null);
		assertNotNull(updatedAuthor);
		assertEquals("Julio Cortázar", updatedAuthor.getFullname());
	}

	@Test
	void deleteAuthor() throws LibraryException {
		int before = this.authorService.getListOfAuthors().size();
		this.authorService.createAuthor("Julio Cortázar", LocalDate.of(1914, 8, 26));
		Author author2 = this.authorService.createAuthor("Jorge Luis Borges", LocalDate.of(1899, 8, 24));
		assertEquals(before + 2, this.authorService.getListOfAuthors().size());
		assertTrue(this.authorService.deleteAuthor(author2.getId()));
		assertEquals(before + 1, this.authorService.getListOfAuthors().size());
		assertFalse(this.authorService.deleteAuthor(author2.getId()));
	}

	@Test
	void getYoungAuthorList() throws LibraryException {
		// "Joven" = menos de 60 anios. Las fechas se calculan respecto de hoy para que el test no caduque.
		LocalDate today = LocalDate.now();
		int before = this.authorService.getYoungAuthors().size();
		this.authorService.createAuthor("Autor de 90", today.minusYears(90));
		this.authorService.createAuthor("Autor de 30", today.minusYears(30));
		this.authorService.createAuthor("Autor de 45", today.minusYears(45));
		this.authorService.createAuthor("Autor de 61", today.minusYears(61));
		this.authorService.createAuthor("Autor de 59", today.minusYears(59));
		assertEquals(before + 3, this.authorService.getYoungAuthors().size());
	}

	@Test
	void getAuthorWithMoreBook() throws LibraryException {
		Author author1 = this.authorService.createAuthor("Julio Cortázar", LocalDate.of(1914, 8, 26));
		Author author2 = this.authorService.createAuthor("Jorge Luis Borges", LocalDate.of(1899, 8, 24));
		bookService.createDigitalBook("9780000000021", "Book Title 1", 2022, "Description 1", "PDF", 10.5f, author1);
		bookService.createDigitalBook("9780000000022", "Book Title 2", 2021, "Description 2", "EPUB", 8.2f, author2);
		bookService.createDigitalBook("9780000000023", "Book Title 3", 2023, "Description 3", "MOBI", 12.0f, author2);
		bookService.createPhysicalBook("9780000000024", "Book Title 4", 2022, "Description 11", 1.2f, "Seccion A", author1);
		bookService.createPhysicalBook("9780000000025", "Book Title 5", 2021, "Description 12", 1.5f, "Seccion B", author1);
		bookService.createPhysicalBook("9780000000026", "Book Title 6", 2020, "Description 13", 1.1f, "Seccion C", author1);
		Author author = this.authorService.getAuthorWithMoreBooks();
		assertEquals(author1.getId(), author.getId());
		assertEquals("Julio Cortázar", author.getFullname());
		assertEquals(4, author.getBooks().size());
	}

	@Test
	void testGetAllBookofAuthor() throws LibraryException {
		Author author1 = this.authorService.createAuthor("Julio Cortázar", LocalDate.of(1914, 8, 26));
		Author author2 = this.authorService.createAuthor("Jorge Luis Borges", LocalDate.of(1899, 8, 24));
		bookService.createPhysicalBook("9780000000031", "Book Title 1", 2022, "Description 11", 1.2f, "Seccion A", author1);
		bookService.createPhysicalBook("9780000000032", "Book Title 2", 2021, "Description 12", 1.5f, "Seccion B", author1);
		bookService.createDigitalBook("9780000000033", "Book Title 1", 2022, "Description 1", "PDF", 10.5f, author1);
		bookService.createDigitalBook("9780000000034", "Book Title 2", 2021, "Description 2", "EPUB", 8.2f, author2);
		bookService.createDigitalBook("9780000000035", "Book Title 3", 2023, "Description 3", "MOBI", 12.0f, author2);

		List<Book> bookList = this.bookService.findByAuthor(author2);
		assertEquals(2, bookList.size());
	}

	// ---------- Consultas JPQL agregadas ----------

	@Test
	void getAuthorWithBooksUsesJoinFetch() throws LibraryException {
		Author author = this.authorService.createAuthor("Silvina Ocampo", LocalDate.of(1903, 7, 28));
		bookService.createPhysicalBook("9780000000041", "Viaje olvidado", 1937, null, 0.3f, "Seccion D", author);
		bookService.createPhysicalBook("9780000000042", "La furia", 1959, null, 0.4f, "Seccion D", author);
		Optional<Author> found = this.authorService.getByIdWithBooks(author.getId());
		assertTrue(found.isPresent());
		assertEquals(2, found.get().getBooks().size());
		assertTrue(this.authorService.getByIdWithBooks(-1L).isEmpty());
	}

	@Test
	void searchAuthorsByName() throws LibraryException {
		this.authorService.createAuthor("Alejandra Pizarnik", LocalDate.of(1936, 4, 29));
		List<Author> found = this.authorService.searchByName("PIZAR");
		assertEquals(1, found.size());
		assertEquals("Alejandra Pizarnik", found.get(0).getFullname());
	}

	@Test
	void getAuthorsWithoutBooks() throws LibraryException {
		Author withBooks = this.authorService.createAuthor("Con libros", LocalDate.of(1950, 1, 1));
		Author withoutBooks = this.authorService.createAuthor("Sin libros", LocalDate.of(1950, 1, 1));
		bookService.createDigitalBook("9780000000051", "Un libro", 2000, null, "PDF", 1.0f, withBooks);
		List<Long> ids = this.authorService.getAuthorsWithoutBooks().stream().map(Author::getId).toList();
		assertTrue(ids.contains(withoutBooks.getId()));
		assertFalse(ids.contains(withBooks.getId()));
	}

	@Test
	void countBooksByAuthorReturnsDto() throws LibraryException {
		Author author = this.authorService.createAuthor("Manuel Puig", LocalDate.of(1932, 12, 28));
		bookService.createPhysicalBook("9780000000061", "Boquitas pintadas", 1969, null, 0.3f, "Seccion E", author);
		bookService.createPhysicalBook("9780000000062", "El beso de la mujer araña", 1976, null, 0.4f, "Seccion E", author);
		AuthorBookCount stats = this.authorService.getBookCountByAuthor().stream()
				.filter(s -> s.id().equals(author.getId()))
				.findFirst().orElseThrow();
		assertEquals("Manuel Puig", stats.fullname());
		assertEquals(2L, stats.books());
	}

	@Test
	void searchBooksByTitle() throws LibraryException {
		Author author = this.authorService.createAuthor("Jorge Luis Borges", LocalDate.of(1899, 8, 24));
		bookService.createPhysicalBook("9780000000071", "El Aleph", 1949, null, 0.3f, "Seccion F", author);
		List<Book> found = this.bookService.searchByTitle("aleph");
		assertEquals(1, found.size());
		assertEquals("9780000000071", found.get(0).getIsbn());
	}

	@Test
	void findBooksByYearRange() throws LibraryException {
		Author author = this.authorService.createAuthor("Jorge Luis Borges", LocalDate.of(1899, 8, 24));
		bookService.createPhysicalBook("9780000000081", "Ficciones", 1944, null, 0.3f, "Seccion F", author);
		bookService.createPhysicalBook("9780000000082", "El hacedor", 1960, null, 0.3f, "Seccion F", author);
		List<String> isbns = this.bookService.findByYearBetween(1940, 1950).stream().map(Book::getIsbn).toList();
		assertTrue(isbns.contains("9780000000081"));
		assertFalse(isbns.contains("9780000000082"));
	}

	@Test
	void findBooksByAuthorFullname() throws LibraryException {
		Author author = this.authorService.createAuthor("Adolfo Bioy Casares", LocalDate.of(1914, 9, 15));
		bookService.createPhysicalBook("9780000000091", "La invención de Morel", 1940, null, 0.3f, "Seccion G", author);
		List<Book> found = this.bookService.findByAuthorFullname("Adolfo Bioy Casares");
		assertEquals(1, found.size());
		assertEquals("La invención de Morel", found.get(0).getTitle());
	}

	@Test
	void findBooksByTypeIsPolymorphic() throws LibraryException {
		Author author = this.authorService.createAuthor("Ricardo Piglia", LocalDate.of(1941, 11, 24));
		bookService.createPhysicalBook("9780000000101", "Respiración artificial", 1980, null, 0.3f, "Seccion H", author);
		bookService.createDigitalBook("9780000000102", "Plata quemada", 1997, null, "EPUB", 2.0f, author);
		List<Book> digital = this.bookService.findByType(DigitalBook.class);
		assertTrue(digital.stream().allMatch(b -> b instanceof DigitalBook));
		assertTrue(digital.stream().anyMatch(b -> b.getIsbn().equals("9780000000102")));
		assertFalse(digital.stream().anyMatch(b -> b.getIsbn().equals("9780000000101")));
		List<DigitalBook> epub = this.bookService.findDigitalBooksByFormat("epub");
		assertTrue(epub.stream().anyMatch(b -> b.getIsbn().equals("9780000000102")));
	}

	@Test
	void averageYearOfAuthor() throws LibraryException {
		Author author = this.authorService.createAuthor("Samanta Schweblin", LocalDate.of(1978, 1, 1));
		assertNull(this.bookService.getAverageYearByAuthor(author));
		bookService.createPhysicalBook("9780000000111", "Pájaros en la boca", 2009, null, 0.3f, "Seccion I", author);
		bookService.createPhysicalBook("9780000000112", "Distancia de rescate", 2014, null, 0.3f, "Seccion I", author);
		bookService.createPhysicalBook("9780000000113", "Kentukis", 2018, null, 0.3f, "Seccion I", author);
		assertEquals(2013.67, this.bookService.getAverageYearByAuthor(author), 0.01);
	}

	@Test
	void bulkUpdateComments() throws LibraryException {
		Author author = this.authorService.createAuthor("Mariana Enriquez", LocalDate.of(1973, 12, 1));
		bookService.createPhysicalBook("9780000000121", "Nuestra parte de noche", 2019, "Sin comentarios", 0.5f, "Seccion J", author);
		assertTrue(this.bookService.updateComments("9780000000121", "Premio Herralde"));
		assertFalse(this.bookService.updateComments("no-existe", "x"));
		Book book = this.bookService.findByIsbn("9780000000121").orElseThrow();
		assertEquals("Premio Herralde", book.getComments());
	}

	@Test
	void nativeQueryMapsToEntity() throws LibraryException {
		Author author = this.authorService.createAuthor("Sara Gallardo", LocalDate.of(1931, 12, 23));
		bookService.createDigitalBook("9780000000131", "Eisejuaz", 1971, null, "PDF", 1.5f, author);
		Optional<Book> found = this.bookService.findByIsbnNative("9780000000131");
		assertTrue(found.isPresent());
		assertInstanceOf(DigitalBook.class, found.get());
		assertEquals("Eisejuaz", found.get().getTitle());
	}

	@Test
	void deleteBook() throws LibraryException {
		Author author = this.authorService.createAuthor("Hebe Uhart", LocalDate.of(1936, 12, 2));
		bookService.createPhysicalBook("9780000000141", "Camilo asciende", 1987, null, 0.3f, "Seccion K", author);
		assertTrue(this.bookService.deleteByIsbn("9780000000141"));
		assertTrue(this.bookService.findByIsbn("9780000000141").isEmpty());
		assertFalse(this.bookService.deleteByIsbn("9780000000141"));
		assertTrue(author.getBooks().isEmpty());
	}

}
