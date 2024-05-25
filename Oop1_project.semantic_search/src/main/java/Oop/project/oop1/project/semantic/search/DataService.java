package Oop.project.oop1.project.semantic.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;


import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * handles business job ; CRUD operations in the code
 */
@Service
public class DataService {

    @Autowired
    private DataRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * add data to db
     * @param data data to add to db
     */
    public void create(Data data) {
        try {
            repository.save(data);

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    /**
     * check for query in db
     * @param userQuery query to search for
     * @return
     */
    public Data getDataByUserQuery(String userQuery) {
        return repository.findByUserQuery(userQuery);

    }

    /**
     * delete data before a nb of months
     * @param months nb of months to delete data before
     */
    public void deleteDataBeforeDuration(int months) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -months);
        Date monthAgo = cal.getTime();

        List<Data> dataToDelete = repository.findByDateBefore(monthAgo);
        repository.deleteAll(dataToDelete);
    }

    /**
     * user adds data to an existing data
     * @param dataId
     * @param value new value to add to db by user
     */
    public void updateResult(String dataId, String value) {
        try {
            Query query = new Query(Criteria.where("_id").is(dataId));
            Update update = new Update().addToSet("result.UserEntered", value).set("date",new Date());
            mongoTemplate.updateFirst(query, update, Data.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
