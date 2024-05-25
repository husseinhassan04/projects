package Oop.project.oop1.project.semantic.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * send http requests and responseto db
 */
@RestController
@RequestMapping("SearchEngine")
public class DataController {
    private final DataService dataService;

    @Autowired

    public DataController(DataService dataService){
        this.dataService=dataService;
    }

    /**
     * add data to db
     * @param data data to enter to db
     */
    @PostMapping("api/addData")
    public void createData(@RequestBody Data data){
        try{
            dataService.create(data);
            System.out.println("Data created");
        }
        catch(Exception e){
            System.err.println("Data not created");
        }
    }

    /**
     * check if data with the query entered exists in db
     * @param userQuery
     * @return data from db
     */
    @GetMapping("/api/data")
    public Data getDataByUserQuery(@PathVariable String userQuery) {
        Data data = dataService.getDataByUserQuery(userQuery);
        if (data!=null) {
            return data;
        } else {
            System.out.println("No data found in database for query: " + userQuery);
            return data;
        }
    }

    /**
     * delete data before a nb of months
     * @param months
     */
    @DeleteMapping("/deleteOlderThan/{months}")
    public void deleteDataOlderThan(@PathVariable int months) {
        dataService.deleteDataBeforeDuration(months);
    }

    /**
     * update exsisting data by adding a data from th user
     * @param dataId
     * @param value new value
     */
    @PutMapping("/api/data/{dataId}/result")
    public void updateResult(@PathVariable String dataId, @RequestParam String value) {
        try {
            dataService.updateResult(dataId, value);
            System.out.println("Result hashmap updated successfully");
        } catch (Exception e) {
            System.err.println("Failed to update result");
        }
    }

}
