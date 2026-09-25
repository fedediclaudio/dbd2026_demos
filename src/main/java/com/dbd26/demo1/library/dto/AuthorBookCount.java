package com.dbd26.demo1.library.dto;

/**
 * DTO (Data Transfer Object) de solo lectura. No es una entidad: se usa como resultado de una consulta JPQL
 * con expresion constructora ("select new ...AuthorBookCount(...)"), ver AuthorRepository.countBooksByAuthor().
 */
public record AuthorBookCount(Long id, String fullname, Long books) {
}
