package com.dbd26.demo1.library.repositories;

import com.dbd26.demo1.library.model.Book;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/*
 MongoTemplate es la API de bajo nivel de Spring Data MongoDB (el equivalente de la Session de Hibernate):
 permite escribir cualquier operacion con Query, Criteria y Update.
 */
public class BookRepositoryImpl implements BookRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    // Actualizacion en la base sin leer el documento: updateOne({ isbn: ... }, { $set: { comments: ... } }).
    // Es atomica por documento. Equivale al @Modifying / createMutationQuery de las otras ramas.
    @Override
    public int updateComments(String isbn, String comments) {
        UpdateResult result = this.mongoTemplate.updateFirst(
                Query.query(where("isbn").is(isbn)),
                Update.update("comments", comments),
                Book.class);
        return (int) result.getModifiedCount();
    }
}
