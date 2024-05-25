package Oop.project.oop1.project.semantic.search;


import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * class for mongodb document
 */
@lombok.Data
@Document(collection = "data")
public class Data {
    @MongoId(FieldType.OBJECT_ID)
    private String  id;
    @Indexed(unique = true)
    private String userQuery;
    private HashMap<String,List<String>> result = new HashMap<String, List<String>>();
    private Date date = new Date();

    public Data(String userQuery){
        this.userQuery = userQuery;

    }

    /**
     *
     * @param fileName
     * @param content
     */
    public void addDataResult(String fileName,List<String> content){
        result.put(fileName,content);
    }


}
