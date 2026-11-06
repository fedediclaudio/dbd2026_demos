package com.dbd26.demo1.library.repositories;

/*
 Fragmento de repositorio: metodos que Spring Data no puede generar solo y se implementan a mano
 en BookRepositoryImpl (Spring Data la detecta por el sufijo "Impl").
 */
public interface BookRepositoryCustom {

    int updateComments(String isbn, String comments);
}
