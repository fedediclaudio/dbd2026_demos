package com.dbd26.demo1.library.repositories;

import com.dbd26.demo1.library.model.DigitalBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 Repositorio sobre una subclase de la jerarquia: todas sus consultas filtran automaticamente por el discriminador
 (type = 'Digital'), aunque la tabla sea la misma que la de Book.
 */
@Repository
public interface DigitalBookRepository extends JpaRepository<DigitalBook, Long> {

    List<DigitalBook> findByFormatIgnoreCase(String format);

}
