# demo_library_2026 · rama `hibernate`
Demo de muestra para el curso Diseño de Bases de Datos (DBD) 2026 - UNLP.

Implementación de referencia de algunas capacidades de Hibernate sobre Spring Boot, **usando la API nativa de
Hibernate directamente** (`SessionFactory`, `Session`, HQL), sin Spring Data. Los repositorios se escriben a mano.

La misma demo, pero con repositorios generados por Spring Data JPA, está en la rama `jpa_repositories`.
Las dos ramas comparten el modelo, los servicios y **exactamente la misma suite de tests**, así que sirven para
comparar qué hace Spring Data por debajo.

La demo se ejecuta a través de los tests: con `hibernate.show_sql` activo se ve en la consola cada SQL
que genera Hibernate. No hay capa web ni API REST, a propósito, para mantener el foco en el mapeo y las consultas.

Las clases `Loan`, `Partner`, `Genre` y `Editorial` están sin mapear a propósito: quedan como actividad
para los alumnos.

## Stack

| Componente            | Versión |
|-----------------------|---------|
| Java                  | 21 (LTS) |
| Spring Boot           | 4.1.1 (solo `starter-jdbc`, sin Spring Data) |
| Spring ORM            | 7.0 (`LocalSessionFactoryBean`, `HibernateTransactionManager`) |
| Hibernate ORM         | 7.4 |
| Jakarta Persistence   | 3.2 |
| MySQL Connector/J     | 9.7 |
| JUnit Jupiter         | 6.0 |

## Requisitos

- JDK 21 o superior.
- MySQL 8.0 o superior, con una base de datos `library_bd` y usuario `root` / `root`
  (ver `src/main/resources/application.properties`).
- No hace falta instalar Maven: el proyecto incluye el Maven Wrapper (`./mvnw` / `mvnw.cmd`).

## Ejecutar los tests

Contra MySQL (comportamiento por defecto). Los tests de `LibraryApplicationTests` usan `@Rollback(false)`,
así que los datos quedan persistidos y se pueden inspeccionar en la base después de correrlos:

```bash
./mvnw test
```

Contra una base H2 en memoria, sin necesidad de tener MySQL levantado:

```bash
./mvnw test -Dspring.profiles.active=h2
```

Un test puntual:

```bash
./mvnw test -Dtest='LibraryApplicationTests#getAuthorWithBooksUsesJoinFetch'
```

## Estructura

| Paquete | Contenido |
|---|---|
| `config` | `HibernateConfiguration`: arma a mano el `SessionFactory` y el `HibernateTransactionManager` |
| `model` | Entidades `Author`, `Book` (abstracta), `PhysicalBook`, `DigitalBook`. Las demás clases son POJOs sin mapear |
| `dto` | `AuthorBookCount`, `record` usado como resultado de una consulta HQL con `select new` |
| `repositories` | Interfaz + implementación a mano (`*RepositoryImpl`) sobre `sessionFactory.getCurrentSession()` |
| `services` | Lógica de negocio y límites transaccionales (`@Transactional`) |

## Cómo se arma Hibernate (`HibernateConfiguration`)

| Pieza | Rol |
|---|---|
| `DataSource` | Pool de conexiones HikariCP. Lo construye Spring Boot desde `spring.datasource.*` |
| `LocalSessionFactoryBean` | Construye el `SessionFactory`: escanea el paquete `model`, recibe el `DataSource` y las propiedades (`hbm2ddl.auto`, `show_sql`) |
| `HibernateTransactionManager` | Hace funcionar `@Transactional`: abre la `Session`, hace commit o rollback, y la deja atada al hilo para `getCurrentSession()` |

## API de `Session` usada en los repositorios

| Método | Qué hace | Equivalente en Hibernate 5 |
|---|---|---|
| `persist(e)` | Pasa la entidad a *managed*; el INSERT se difiere al flush (salvo IDENTITY) | `save(e)` |
| `merge(e)` | Copia el estado de una entidad *detached* sobre la *managed* y la devuelve. Chequea `@Version` | `update(e)` / `saveOrUpdate(e)` |
| `remove(e)` | Marca para borrado; el DELETE se ejecuta en el flush | `delete(e)` |
| `find(Clase, id)` | Busca por PK, primero en la caché de primer nivel | `get(Clase, id)` |
| `createSelectionQuery(hql, Clase)` | Consulta HQL tipada. `list()`, `uniqueResult()`, `uniqueResultOptional()` | `createQuery(hql)` sin tipo |
| `createMutationQuery(hql)` | `update` / `delete` masivo, `executeUpdate()` | `createQuery(hql).executeUpdate()` |
| `createNativeQuery(sql, Clase)` | SQL nativo mapeado a la entidad | `createSQLQuery(sql)` |
| `flush()` / `clear()` | Sincroniza con la base / vacía la caché de primer nivel | igual |

## Consultas de ejemplo en los repositorios

| Repositorio | Método | Qué muestra |
|---|---|---|
| `AuthorRepositoryImpl` | `findById` | `find()` por PK, sin SQL si la entidad ya está en la `Session` |
| `AuthorRepositoryImpl` | `findByDateOfBirthGreaterThan`, `findByFullnameContainingIgnoreCase` | HQL con parámetros nombrados, `like` + `concat` + `lower` |
| `AuthorRepositoryImpl` | `findByMoreBooks` | `join` + `group by` + `order by count()` |
| `AuthorRepositoryImpl` | `findByIdWithBooks` | `join fetch` para evitar el problema N+1, `uniqueResultOptional()` |
| `AuthorRepositoryImpl` | `findWithoutBooks` | Predicado `is empty` sobre una colección |
| `AuthorRepositoryImpl` | `countBooksByAuthor` | Proyección a un DTO (`record`) con `select new ...` |
| `BookRepositoryImpl` | `findAll`, `findAllDigitalBooks` | Consultas polimórficas: sobre la raíz y sobre una subclase |
| `BookRepositoryImpl` | `findByType` | `type(b)` parametrizado con la clase |
| `BookRepositoryImpl` | `findByYearBetween` | Parámetros nombrados y `between` |
| `BookRepositoryImpl` | `findByAuthorId`, `findByAuthorFullname` | Navegación por asociación (`b.author.fullname`) |
| `BookRepositoryImpl` | `searchByTitle` | `like` + `join fetch` del autor |
| `BookRepositoryImpl` | `averageYearByAuthor` | Función de agregación escalar (`avg`) con `uniqueResult()` |
| `BookRepositoryImpl` | `updateComments` | `createMutationQuery` con `flush()` antes y `clear()` después |
| `BookRepositoryImpl` | `findByIsbnNative` | SQL nativo mapeado a la entidad |

## Tests

| Clase | Qué muestra |
|---|---|
| `LibraryApplicationTests` | Alta, consulta, modificación y baja a través de los servicios, y cada una de las consultas de la tabla anterior. Toda la clase corre en una transacción por test |
| `LibraryConcurrencyTests` | Sin `@Transactional`: bloqueo optimista con `@Version` entre dos transacciones (`merge` con versión vieja), y violación de FK al borrar un autor con libros |
