package com.dbd26.demo1.library.repositories;

import com.dbd26.demo1.library.dto.AuthorBookCount;
import com.dbd26.demo1.library.model.Author;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/*
 @Repository: ademas de registrar el bean, hace que las excepciones de Hibernate se traduzcan a la jerarquia
 de excepciones de acceso a datos de Spring (DataAccessException), independiente del ORM que se use.
 */
@Repository
public class AuthorRepositoryImpl implements AuthorRepository {

    @Autowired
    private SessionFactory sessionFactory;

    /*
     La Session "actual" es la que abrio el HibernateTransactionManager para la transaccion en curso
     (ver HibernateConfiguration). Fuera de una transaccion, getCurrentSession() falla.
     */
    private Session session() {
        return this.sessionFactory.getCurrentSession();
    }

    // persist(): la entidad pasa a estado "managed". Con @GeneratedValue IDENTITY el INSERT se ejecuta en el acto
    // (Hibernate necesita el id que genera la base); con otras estrategias se difiere hasta el flush/commit.
    // En Hibernate 7 ya no existe session.save().
    @Override
    public void save(Author author) {
        this.session().persist(author);
    }

    // merge(): copia el estado de una entidad "detached" (por ejemplo una que viene de otra transaccion) sobre la
    // instancia managed y devuelve esta ultima. Si la @Version recibida es vieja, lanza StaleObjectStateException.
    // En Hibernate 7 ya no existe session.update().
    @Override
    public Author update(Author author) {
        return this.session().merge(author);
    }

    // remove(): marca la entidad para borrado; el DELETE se ejecuta en el flush/commit.
    // En Hibernate 7 ya no existe session.delete().
    @Override
    public void delete(Author author) {
        this.session().remove(author);
    }

    // find() por clave primaria: primero busca en la cache de primer nivel (la propia Session); si la entidad ya
    // fue cargada en esta transaccion no ejecuta ninguna SQL.
    @Override
    public Optional<Author> findById(Long id) {
        return Optional.ofNullable(this.session().find(Author.class, id));
    }

    @Override
    public boolean existsById(Long id) {
        Long count = this.session()
                .createSelectionQuery("select count(a) from Author a where a.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
        return count > 0;
    }

    // HQL: se consulta sobre entidades y atributos, no sobre tablas y columnas. "from Author" es la forma corta
    // de "select a from Author a".
    @Override
    public List<Author> findAll() {
        return this.session()
                .createSelectionQuery("from Author", Author.class)
                .list();
    }

    // Parametros nombrados (:date): nunca concatenar valores en el HQL (SQL injection + sin cache de sentencias).
    @Override
    public List<Author> findByDateOfBirthGreaterThan(LocalDate date) {
        return this.session()
                .createSelectionQuery("from Author a where a.dateOfBirth > :date", Author.class)
                .setParameter("date", date)
                .list();
    }

    // "like" con concat y lower, para busqueda parcial insensible a mayusculas.
    @Override
    public List<Author> findByFullnameContainingIgnoreCase(String text) {
        return this.session()
                .createSelectionQuery("from Author a where lower(a.fullname) like lower(concat('%', :text, '%'))", Author.class)
                .setParameter("text", text)
                .list();
    }

    // join + group by + funcion de agregacion: autores ordenados por cantidad de libros (solo los que tienen alguno).
    @Override
    public List<Author> findByMoreBooks() {
        return this.session()
                .createSelectionQuery("select a from Author a join a.books b group by a.id order by count(b) desc", Author.class)
                .list();
    }

    // JOIN FETCH: trae el autor y su coleccion de libros en UNA sola consulta, en lugar de la carga LAZY posterior
    // (evita el problema "N+1"). uniqueResultOptional(): 0 o 1 resultado, falla si hay mas de uno.
    @Override
    public Optional<Author> findByIdWithBooks(Long id) {
        return this.session()
                .createSelectionQuery("select a from Author a left join fetch a.books where a.id = :id", Author.class)
                .setParameter("id", id)
                .uniqueResultOptional();
    }

    // Predicado "is empty" sobre una coleccion (equivale a un "not exists" en SQL).
    @Override
    public List<Author> findWithoutBooks() {
        return this.session()
                .createSelectionQuery("from Author a where a.books is empty", Author.class)
                .list();
    }

    // Proyeccion con expresion constructora: el resultado no es una entidad sino un DTO (record).
    // "left join" para que aparezcan tambien los autores con cero libros.
    @Override
    public List<AuthorBookCount> countBooksByAuthor() {
        return this.session()
                .createSelectionQuery(
                        "select new com.dbd26.demo1.library.dto.AuthorBookCount(a.id, a.fullname, count(b)) " +
                        "from Author a left join a.books b " +
                        "group by a.id, a.fullname " +
                        "order by count(b) desc, a.fullname", AuthorBookCount.class)
                .list();
    }
}
