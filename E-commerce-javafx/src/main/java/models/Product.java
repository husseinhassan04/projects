package models;

public class Product {
    private int id;
    private String name;
    private String desc;
    private double price;
    private int stock;
    private String imageUrl;
    private double rating;

    public Product(int id, String name,String description, double price, int stock, String imageUrl) {
        this.id = id;
        this.name = name;
        this.desc = description;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
    }

    public int getId() {
        return id;
    }

    public double getRating(){
        return rating;
    }
    public void setRating(double rating){
        this.rating = rating;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return desc;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }


    public int getStock() {
        return stock;
    }


    public String getImageUrl() {
        return imageUrl;
    }


    @Override
    public String toString() {
        return "Product{" + "id=" + id + ", name='" + name + '\'' + ", price=" + price +
                ", stock=" + stock + ", imageUrl='" + imageUrl + '\'' + '}';
    }
}
