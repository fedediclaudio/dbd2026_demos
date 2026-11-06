package com.dbd26.demo1.library.dto;

// DTO de solo lectura: resultado de la agregacion de AuthorRepository.countBooksByAuthor(), mapeado por nombre.
public record AuthorBookCount(String id, String fullname, Long books) {
}
