package models;

import javafx.beans.property.*;

import java.util.ArrayList;
import java.util.List;

public class Order {

    private final IntegerProperty id;
    private final StringProperty status;
    private final DoubleProperty total;
    private final StringProperty trackingNumber;
    private ArrayList<Product> products;

    public Order(int id, String status, double total, String trackingNumber) {
        this.id = new SimpleIntegerProperty(id);
        this.status = new SimpleStringProperty(status);
        this.total = new SimpleDoubleProperty(total);
        this.trackingNumber = new SimpleStringProperty(trackingNumber);
        this.products = new ArrayList<Product>();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty statusProperty() {
        return status;
    }


    public DoubleProperty totalProperty() {
        return total;
    }



    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }


    public double getTotal() {
        return total.get();
    }


    public String getTrackingNumber() {
        return trackingNumber.get();
    }


    public ArrayList<Product> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<Product> products) {
        this.products = products;
    }
}
