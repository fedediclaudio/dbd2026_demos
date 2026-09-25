# demo_library_2026
Demo de muestra para el curso Diseño de Bases de Datos (DBD) 2026 - UNLP.

Implementación de referencia de algunas capacidades de Hibernate / JPA sobre Spring Boot:
entidades, herencia (`SINGLE_TABLE`), relaciones `@OneToMany` / `@ManyToOne`, carga `LAZY` / `EAGER`,
bloqueo optimista (`@Version`), repositorios Spring Data, consultas derivadas, JPQL y SQL nativo.

La demo se ejecuta a través de los tests: con `spring.jpa.show-sql=true` se ve en la consola cada SQL
que genera Hibernate. No hay capa web ni API REST, a propósito, para mantener el foco en el mapeo y las consultas.

Las clases `Loan`, `Partner`, `Genre` y `Editorial` están sin mapear a propósito: quedan como actividad
para los alumnos.

## Stack

| Componente            | Versión |
|-----------------------|---------|
| Java                  | 21 (LTS) |
| Spring Boot           | 4.1.1 |
| Spring Data JPA       | 2026.0 |
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
| `model` | Entidades `Author`, `Book` (abstracta), `PhysicalBook`, `DigitalBook`. Las demás clases son POJOs sin mapear |
| `dto` | `AuthorBookCount`, `record` usado como resultado de una consulta JPQL con `select new` |
| `repositories` | Interfaces Spring Data con consultas derivadas, JPQL, nativas y `@Modifying` |
| `services` | Lógica de negocio y límites transaccionales (`@Transactional`) |

## Consultas de ejemplo en los repositorios

| Repositorio | Método | Qué muestra |
|---|---|---|
| `AuthorRepository` | `findByDateOfBirthGreaterThan`, `findByFullnameContainingIgnoreCase` | Consultas derivadas del nombre del método |
| `AuthorRepository` | `findByMoreBooks` | `join` + `group by` + `order by count()` |
| `AuthorRepository` | `findByIdWithBooks` | `join fetch` para evitar el problema N+1 |
| `AuthorRepository` | `findWithoutBooks` | Predicado `is empty` sobre una colección |
| `AuthorRepository` | `countBooksByAuthor` | Proyección a un DTO (`record`) con `select new ...` |
| `BookRepository` | `findByYearBetween` | Parámetros nombrados y `between` |
| `BookRepository` | `searchByTitle` | `like` + `concat` + `lower` |
| `BookRepository` | `findByAuthorFullname` | Navegación por asociación (`b.author.fullname`) |
| `BookRepository` | `findByType` | Consulta polimórfica con `type(b)` sobre la jerarquía |
| `BookRepository` | `averageYearByAuthor` | Función de agregación escalar (`avg`) |
| `BookRepository` | `updateComments` | Actualización masiva con `@Modifying` |
| `BookRepository` | `findByIsbnNative` | SQL nativo (`nativeQuery = true`) |
| `DigitalBookRepository` | `findByFormatIgnoreCase` | Repositorio sobre una subclase (filtra por discriminador) |

## Tests

| Clase | Qué muestra |
|---|---|
| `LibraryApplicationTests` | Alta, consulta, modificación y baja a través de los servicios, y cada una de las consultas de la tabla anterior. Toda la clase corre en una transacción por test |
| `LibraryConcurrencyTests` | Sin `@Transactional`: bloqueo optimista con `@Version` entre dos transacciones, y violación de FK al borrar un autor con libros |
