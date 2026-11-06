# demo_library_2026 · rama `mongo`
Demo de muestra para el curso Diseño de Bases de Datos (DBD) 2026 - UNLP.

La misma biblioteca de las ramas `jpa_repositories` y `hibernate`, pero sobre **MongoDB con Spring Data MongoDB**:
documentos en lugar de tablas, filtros JSON y pipelines de agregación en lugar de SQL/JPQL, y sin ORM.

Las tres ramas comparten el modelo de dominio, las interfaces de servicio y la suite de tests, con las pocas
diferencias que impone una base de documentos (ver más abajo). Sirven para comparar qué cambia al pasar de una
base relacional a una documental.

La demo se ejecuta a través de los tests: con el log de `MongoTemplate` en `DEBUG` se ve en la consola cada
operación que se envía a MongoDB. No hay capa web ni API REST.

Las clases `Loan`, `Partner`, `Genre` y `Editorial` están sin mapear a propósito: quedan como actividad
para los alumnos. En MongoDB es un buen ejercicio decidir qué embeber y qué referenciar.

## Stack

| Componente            | Versión |
|-----------------------|---------|
| Java                  | 21 (LTS) |
| Spring Boot           | 4.1.1 |
| Spring Data MongoDB   | 2026.0 |
| MongoDB Java Driver   | 5.8 |
| MongoDB               | 8.x (vale 4.4 o superior) |
| JUnit Jupiter         | 6.0 |

## Requisitos

- JDK 21 o superior.
- MongoDB accesible en `localhost:27017`, sin autenticación (ver `src/main/resources/application.properties`;
  en Boot 4 la propiedad es `spring.mongodb.uri`, ya no `spring.data.mongodb.uri`).
  Con Docker:

  ```bash
  docker run -d --name mongo-tp -p 27017:27017 mongo:8
  ```

- No hace falta instalar Maven: el proyecto incluye el Maven Wrapper (`./mvnw` / `mvnw.cmd`).

## Ejecutar los tests

```bash
./mvnw test
```

Los datos quedan persistidos en la base `library_bd` (la suite la vacía al empezar, no al terminar).
Para mirarlos:

```bash
mongosh library_bd
db.book.find().pretty()
db.author.find().pretty()
```

Un test puntual:

```bash
./mvnw test -Dtest='LibraryApplicationTests#findBooksByTypeIsPolymorphic'
```

## Cómo se modeló

| Concepto relacional | En esta rama |
|---|---|
| Tabla `author` / `book` | Colecciones `author` / `book` (`@Document`) |
| Clave primaria autoincremental | `_id` de tipo `ObjectId`, generado por MongoDB, como `String` en Java |
| Herencia `SINGLE_TABLE` + discriminador `type` | Una sola colección `book` + campo `_class` con `@TypeAlias("Physical")` / `("Digital")` |
| `@ManyToOne` con FK `id_author` | `@DocumentReference` en `Book.author`: guarda solo el `_id` del autor |
| `@OneToMany(mappedBy)` LAZY | `@ReadOnlyProperty @DocumentReference(lookup = …, lazy = true)` en `Author.books`: no se guarda, se consulta al acceder |
| Restricción `unique` sobre `isbn` | Índice único `@Indexed(unique = true)` |
| Clave foránea | **No existe.** El servicio impide borrar un autor con libros |
| `@Version` (bloqueo optimista) | Igual, con `Integer` en lugar de `int` |
| `@Transactional` | Se quita: atomicidad por documento. Transacciones multi-documento requieren replica set |
| `ddl-auto=create` | No hay esquema. La suite vacía la base en `@BeforeAll` |

## Consultas de ejemplo en los repositorios

| Repositorio | Método | Qué muestra |
|---|---|---|
| `AuthorRepository` | `findByDateOfBirthGreaterThan`, `findByFullnameContainingIgnoreCase` | Consultas derivadas, traducidas a `$gt` y a expresión regular |
| `AuthorRepository` | `findByMoreBooks` | `@Aggregation` con `$lookup` (el "join" de MongoDB), `$addFields`, `$match`, `$sort` |
| `AuthorRepository` | `findWithoutBooks` | `$lookup` + `$match` con `$size: 0` |
| `AuthorRepository` | `countBooksByAuthor` | `$project` que arma un DTO (`record`) |
| `BookRepository` | `findByIsbnNative` | `@Query` con el filtro JSON tal cual se escribe en `mongosh` |
| `BookRepository` | `findByYearBetween` | `$gte` / `$lte` con orden |
| `BookRepository` | `searchByTitle` | `$regex` con `$options: 'i'` |
| `BookRepository` | `findByTypeAlias` | Consulta polimórfica por `_class` |
| `BookRepository` | `findByAuthor` | Consulta derivada sobre una referencia |
| `BookRepository` | `findByAuthorFullname` | `$lookup` hacia `author` para filtrar por un campo del autor |
| `BookRepository` | `averageYearByAuthor` | `$group` + `$avg`, resultado escalar |
| `BookRepositoryImpl` | `updateComments` | Fragmento a mano con `MongoTemplate`: `updateFirst` + `$set` |
| `DigitalBookRepository` | `findAllDigital`, `findByFormatIgnoreCase` | Repositorio de subclase: el filtro por `_class` hay que escribirlo |

## Tests

| Clase | Qué muestra |
|---|---|
| `LibraryApplicationTests` | Misma suite que las otras ramas. Diferencias: sin `@Transactional`, vacía la base al inicio, y donde JPA garantizaba identidad de objetos acá hay que volver a leer |
| `LibraryConcurrencyTests` | Bloqueo optimista con `@Version` (funciona igual) y la ausencia de claves foráneas: es el servicio quien rechaza borrar un autor con libros |
