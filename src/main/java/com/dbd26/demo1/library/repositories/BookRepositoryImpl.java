package com.dbd26.demo1.library.repositories;

import com.dbd26.demo1.library.model.Book;
import com.dbd26.demo1.library.model.DigitalBook;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BookRepositoryImpl implements BookRepository {

    @Autowired
    private SessionFactory sessionFactory;

    private Session session() {
        return this.sessionFactory.getCurrentSession();
    }

    @Override
    public void save(Book book) {
        this.session().persist(book);
    }

    @Override
    public void delete(Book book) {
        this.session().remove(book);
    }

    // Consulta polimorfica: "from Book" devuelve fisicos y digitales, cada uno instanciado con su subclase
    // segun la columna discriminadora "type".
    @Override
    public List<Book> findAll() {
        return this.session()
                .createSelectionQuery("from Book", Book.class)
                .list();
    }

    // HQL sobre la subclase: Hibernate agrega automaticamente el filtro por discriminador (type = 'Digital').
    @Override
    public List<DigitalBook> findAllDigitalBooks() {
        return this.session()
                .createSelectionQuery("from DigitalBook", DigitalBook.class)
                .list();
    }

    @Override
    public List<DigitalBook> findDigitalBooksByFormat(String format) {
        return this.session()
                .createSelectionQuery("from DigitalBook d where lower(d.format) = lower(:format)", DigitalBook.class)
                .setParameter("format", format)
                .list();
    }

    // "type(b)" filtra por subclase de forma parametrizable (se pasa PhysicalBook.class o DigitalBook.class).
    @Override
    public List<Book> findByType(Class<? extends Book> type) {
        return this.session()
                .createSelectionQuery("from Book b where type(b) = :type", Book.class)
                .setParameter("type", type)
                .list();
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return this.session()
                .createSelectionQuery("from Book b where b.isbn = :isbn", Book.class)
                .setParameter("isbn", isbn)
                .uniqueResultOptional();
    }

    // Consulta SQL nativa (no HQL): se escribe contra las tablas y columnas reales. Al indicar la clase,
    // Hibernate mapea igualmente cada fila a la entidad (usando el discriminador para elegir la subclase).
    @Override
    public Optional<Book> findByIsbnNative(String isbn) {
        return this.session()
                .createNativeQuery("select * from book where isbn = :isbn", Book.class)
                .setParameter("isbn", isbn)
                .uniqueResultOptional();
    }

    // Navegacion por asociacion (b.author.id): Hibernate resuelve el join con la tabla author.
    @Override
    public List<Book> findByAuthorId(Long authorId) {
        return this.session()
                .createSelectionQuery("from Book b where b.author.id = :authorId", Book.class)
                .setParameter("authorId", authorId)
                .list();
    }

    @Override
    public List<Book> findByAuthorFullname(String fullname) {
        return this.session()
                .createSelectionQuery("from Book b where b.author.fullname = :fullname", Book.class)
                .setParameter("fullname", fullname)
                .list();
    }

    // Parametros nombrados y "between".
    @Override
    public List<Book> findByYearBetween(int from, int to) {
        return this.session()
                .createSelectionQuery("from Book b where b.year between :from and :to order by b.year, b.title", Book.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .list();
    }

    // Busqueda parcial con "like" + "concat", y "join fetch" del autor para traerlo en la misma consulta.
    @Override
    public List<Book> searchByTitle(String text) {
        return this.session()
                .createSelectionQuery("select b from Book b join fetch b.author where lower(b.title) like lower(concat('%', :text, '%'))", Book.class)
                .setParameter("text", text)
                .list();
    }

    // Funcion de agregacion escalar: el resultado es un unico valor, no una entidad. uniqueResult() devuelve null
    // si el autor no tiene libros.
    @Override
    public Double averageYearByAuthor(Long authorId) {
        return this.session()
                .createSelectionQuery("select avg(b.year) from Book b where b.author.id = :authorId", Double.class)
                .setParameter("authorId", authorId)
                .uniqueResult();
    }

    // Operacion masiva (bulk update): un unico UPDATE directo en la base, sin pasar por las entidades en memoria.
    @Override
    public int updateComments(String isbn, String comments) {
        Session session = this.session();
        // Se envian antes los cambios pendientes de la Session, para que el UPDATE los vea
        // (es lo que hace flushAutomatically en Spring Data).
        session.flush();
        int updated = session
                .createMutationQuery("update Book b set b.comments = :comments where b.isbn = :isbn")
                .setParameter("isbn", isbn)
                .setParameter("comments", comments)
                .executeUpdate();
        // Como el UPDATE no paso por las entidades, las que ya estaban cargadas quedaron desactualizadas:
        // se limpia la Session para que la proxima consulta las vuelva a leer de la base (clearAutomatically).
        session.clear();
        return updated;
    }
}
