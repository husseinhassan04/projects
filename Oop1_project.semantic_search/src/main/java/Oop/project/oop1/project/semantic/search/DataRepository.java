package Oop.project.oop1.project.semantic.search;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * create a repository for the data class to enter attributes to db
 * extends the mongorepository which makes it able to use the mongo CRUD functions
 */
@Repository
public interface DataRepository extends MongoRepository<Data, String> {
    Data findByUserQuery(String userQuery);

    List<Data> findByDateBefore(Date date);



}
