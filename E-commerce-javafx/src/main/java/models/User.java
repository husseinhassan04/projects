package models;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class User {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty username;
    private final SimpleStringProperty role;
    private final SimpleStringProperty email;
    private final SimpleStringProperty address;
    private final SimpleStringProperty paymentMethod;

    public User(int id, String username, String role, String email, String address, String paymentMethod) {
        this.id = new SimpleIntegerProperty(id);
        this.username = new SimpleStringProperty(username);
        this.role = new SimpleStringProperty(role);
        this.email = new SimpleStringProperty(email);
        this.address = new SimpleStringProperty(address);
        this.paymentMethod = new SimpleStringProperty(paymentMethod);
    }

    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getUsername() {
        return username.get();
    }

    public SimpleStringProperty usernameProperty() {
        return username;
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public String getRole() {
        return role.get();
    }

    public SimpleStringProperty roleProperty() {
        return role;
    }


    public String getEmail() {
        return email.get();
    }

    public SimpleStringProperty emailProperty() {
        return email;
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public String getAddress() {
        return address.get();
    }

    public SimpleStringProperty addressProperty() {
        return address;
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public String getPaymentMethod() {
        return paymentMethod.get();
    }

    public SimpleStringProperty paymentMethodProperty() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod.set(paymentMethod);
    }
}
