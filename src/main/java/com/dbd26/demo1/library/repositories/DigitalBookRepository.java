package com.dbd26.demo1.library.repositories;

import com.dbd26.demo1.library.model.DigitalBook;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

// Comparte la coleccion "book" con PhysicalBook: hay que filtrar por "_class" a mano, Spring Data no lo agrega.
@Repository
public interface DigitalBookRepository extends MongoRepository<DigitalBook, String> {

    @Query("{ '_class': 'Digital' }")
    List<DigitalBook> findAllDigital();

    @Query("{ '_class': 'Digital', 'format': { $regex: ?0, $options: 'i' } }")
    List<DigitalBook> findByFormatIgnoreCase(String format);
}
