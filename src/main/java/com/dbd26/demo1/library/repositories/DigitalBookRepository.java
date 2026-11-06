package com.dbd26.demo1.library.repositories;

import com.dbd26.demo1.library.model.DigitalBook;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 Repositorio sobre una subclase. Comparte la coleccion "book" con PhysicalBook, asi que las consultas deben
 filtrar por "_class" explicitamente: a diferencia de Hibernate, Spring Data MongoDB no agrega el discriminador solo.
 */
@Repository
public interface DigitalBookRepository extends MongoRepository<DigitalBook, String> {

    @Query("{ '_class': 'Digital' }")
    List<DigitalBook> findAllDigital();

    @Query("{ '_class': 'Digital', 'format': { $regex: ?0, $options: 'i' } }")
    List<DigitalBook> findByFormatIgnoreCase(String format);
}
