package models;

public class ShippingProvider {
    private int id;
    private String name;
    private double price;

    public ShippingProvider(int id,String name, double price) {
        this.name = name;
        this.price = price;
        this.id = id;
    }
    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return name + " ($" + price + ")";
    }
}
