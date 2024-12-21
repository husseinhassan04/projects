package models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

public class CartItem {
    private ObjectProperty<Product> product;
    private IntegerProperty quantity;

    public CartItem(Product product, int quantity) {
        this.product = new SimpleObjectProperty<>(product);
        this.quantity = new SimpleIntegerProperty(quantity);
    }

    public Product getProduct() {
        return product.get();
    }

    public void setProduct(Product product) {
        this.product.set(product);
    }

    public int getQuantity() {
        return quantity.get();
    }


    @Override
    public String toString() {
        return getProduct().getName() + " (x" + getQuantity() + ")";
    }
}
