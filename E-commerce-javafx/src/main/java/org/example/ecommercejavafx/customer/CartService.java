package org.example.ecommercejavafx.customer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import models.CartItem;
import models.Product;
import models.ShippingProvider;
import org.example.ecommercejavafx.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartService {
    private List<CartItem> cart;
    private int userId;

    public CartService(int userId) {
        this.userId = userId;
        this.cart = new ArrayList<>();
        loadCartFromDb();
    }

    private void loadCartFromDb() {
        try (Connection connection = DbConnection.getConnection()) {
            ResultSet resultSet = DbConnection.loadCartItems(connection, userId);

            while (resultSet.next()) {
                int productId = resultSet.getInt("product_id");
                int quantity = resultSet.getInt("quantity");
                String productName = resultSet.getString("name");
                String desc = resultSet.getString("description");
                double productPrice = resultSet.getDouble("price");
                int productStock = resultSet.getInt("stock");
                String productImageUrl = resultSet.getString("image_url");

                Product product = new Product(productId, productName, desc, productPrice, productStock, productImageUrl);
                CartItem cartItem = new CartItem(product, quantity);
                cart.add(cartItem);
            }
        } catch (SQLException e) {
            showAlert("Database Error: An error occurred while loading the cart.");
            e.printStackTrace();
        }
    }

    public VBox viewCart() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Label title = new Label("Your Cart");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");

        ListView<CartItem> cartListView = new ListView<>();
        cartListView.setCellFactory(param -> new ListCell<CartItem>() {
            @Override
            protected void updateItem(CartItem item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    HBox hBox = new HBox(15);
                    hBox.setPrefHeight(120);
                    hBox.setAlignment(Pos.CENTER_LEFT);
                    hBox.setStyle("-fx-padding: 10; -fx-border-color: lightgray; -fx-border-width: 2; -fx-background-color: #ffffff; -fx-border-radius: 10; -fx-background-radius: 10;");

                    Product product = item.getProduct();
                    ImageView imageView = new ImageView(new Image(product.getImageUrl()));
                    imageView.setFitHeight(80);
                    imageView.setFitWidth(80);
                    imageView.setPreserveRatio(true);
                    imageView.setStyle("-fx-effect: dropshadow(gaussian, gray, 10, 0.5, 1, 1);");

                    VBox vBox = new VBox(5);
                    Text name = new Text(product.getName());
                    name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
                    Text price = new Text("Price: $" + product.getPrice());
                    price.setStyle("-fx-font-size: 14px;");
                    Text quantity = new Text("Quantity: " + item.getQuantity());
                    quantity.setStyle("-fx-font-size: 14px;");
                    vBox.getChildren().addAll(name, price, quantity);

                    Button removeButton = new Button("❌");
                    removeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-font-size: 16px;");
                    removeButton.setOnAction(event -> {
                        removeFromCart(item);
                        cartListView.getItems().remove(item);
                    });

                    removeButton.setOnMouseEntered(event -> removeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: darkred; -fx-font-size: 16px;"));
                    removeButton.setOnMouseExited(event -> removeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-font-size: 16px;"));

                    hBox.getChildren().addAll(imageView, vBox, removeButton);
                    setGraphic(hBox);
                } else {
                    setGraphic(null);
                }
            }
        });

        cartListView.getItems().addAll(cart);

        Button checkoutButton = new Button("Proceed to Checkout");
        checkoutButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-font-size: 16px; -fx-border-radius: 5px;");
        checkoutButton.setOnAction(event -> checkout(cartListView));

        Text totalPriceText = new Text("Total: $" + calculateTotalPrice());
        totalPriceText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");

        layout.getChildren().addAll(title, cartListView, totalPriceText, checkoutButton);
        layout.prefWidthProperty().bind(cartListView.widthProperty());
        return layout;
    }

    public void addToCart(Product product, int quantity) {
        CartItem item = new CartItem(product, quantity);
        cart.add(item);
        saveToDb(item);
    }

    private void saveToDb(CartItem item) {
        try (Connection connection = DbConnection.getConnection()) {
            DbConnection.saveCartItem(connection, userId, item);
        } catch (SQLException e) {
            showAlert("Database Error: An error occurred while saving the item.");
            e.printStackTrace();
        }
    }

    public void removeFromCart(CartItem item) {
        cart.remove(item);
        try (Connection connection = DbConnection.getConnection()) {
            DbConnection.removeCartItem(connection, userId, item);
        } catch (SQLException e) {
            showAlert("Database Error: An error occurred while removing the item from the cart.");
            e.printStackTrace();
        }
    }

    private void checkout(ListView<CartItem> cartListView) {
        if (cart.isEmpty()) {
            showAlert("Your cart is empty! Add items before proceeding.");
            return;
        }

        double totalPrice = calculateTotalPrice();
        new ShippingPage(userId, totalPrice, this).show();
    }

    public void completeCheckout(ShippingProvider provider, Double finalPrice) {
        try (Connection connection = DbConnection.getConnection()) {
            DbConnection.completeCheckout(connection, userId, finalPrice, provider, cart);
            cart.clear();
            clearCartDb();
            showAlert("Checkout completed! Total Price: $" + finalPrice);
        } catch (SQLException e) {
            showAlert("Error during checkout: " + e.getMessage());
        }
    }

    private void clearCartDb() {
        try (Connection connection = DbConnection.getConnection()) {
            DbConnection.clearCart(connection, userId);
        } catch (SQLException e) {
            showAlert("Database Error: An error occurred while clearing the cart.");
            e.printStackTrace();
        }
    }

    private double calculateTotalPrice() {
        double total = 0;
        for (CartItem item : cart) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }
        return total;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Cart Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
