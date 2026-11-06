package com.dbd26.demo1.library.dto;

/**
 * DTO (Data Transfer Object) de solo lectura. No es un documento: se usa como resultado de una agregacion
 * ($lookup + $project), ver AuthorRepository.countBooksByAuthor(). Spring Data mapea los campos del documento
 * resultante a los componentes del record por nombre.
 */
public record AuthorBookCount(String id, String fullname, Long books) {
}
